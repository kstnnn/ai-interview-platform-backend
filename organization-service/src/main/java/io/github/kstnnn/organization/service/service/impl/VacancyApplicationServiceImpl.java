package io.github.kstnnn.organization.service.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import io.github.kstnnn.organization.service.dto.AiCustomQuestionRequest;
import io.github.kstnnn.organization.service.dto.AiInterviewReportDto;
import io.github.kstnnn.organization.service.dto.AiInterviewQuestionReportDto;
import io.github.kstnnn.organization.service.dto.AiTopicStateSummaryDto;
import io.github.kstnnn.organization.service.dto.AiStartInterviewRequest;
import io.github.kstnnn.organization.service.dto.CandidateContactsDto;
import io.github.kstnnn.organization.service.dto.EmployerApplicationReportDto;
import io.github.kstnnn.organization.service.dto.EmployerCandidateDto;
import io.github.kstnnn.organization.service.dto.EmployerVacancyApplicationResponse;
import io.github.kstnnn.organization.service.dto.VacancyApplicationResponse;
import io.github.kstnnn.organization.service.dto.VacancyApplyRequest;
import io.github.kstnnn.organization.service.exception.DuplicateApplicationException;
import io.github.kstnnn.organization.service.exception.InvalidApplicationRequestException;
import io.github.kstnnn.organization.service.exception.ResourceNotFoundException;
import io.github.kstnnn.organization.service.model.Vacancy;
import io.github.kstnnn.organization.service.model.VacancyApplication;
import io.github.kstnnn.organization.service.model.VacancyApplicationStatus;
import io.github.kstnnn.organization.service.model.VacancyStatus;
import io.github.kstnnn.organization.service.repository.VacancyApplicationRepository;
import io.github.kstnnn.organization.service.repository.VacancyQuestionRepository;
import io.github.kstnnn.organization.service.repository.VacancyRepository;
import io.github.kstnnn.organization.service.repository.VacancyTechnologyRepository;
import io.github.kstnnn.organization.service.service.AiInterviewClient;
import io.github.kstnnn.organization.service.service.CurrentUserService;
import io.github.kstnnn.organization.service.service.OrganizationAccessService;
import io.github.kstnnn.organization.service.service.VacancyApplicationService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class VacancyApplicationServiceImpl implements VacancyApplicationService {

  private final VacancyApplicationRepository vacancyApplicationRepository;
  private final VacancyRepository vacancyRepository;
  private final VacancyQuestionRepository vacancyQuestionRepository;
  private final VacancyTechnologyRepository vacancyTechnologyRepository;
  private final CurrentUserService currentUserService;
  private final OrganizationAccessService organizationAccessService;
  private final AiInterviewClient aiInterviewClient;

  @Override
  @Transactional
  public VacancyApplicationResponse apply(Jwt jwt, UUID vacancyId, VacancyApplyRequest request) {
    var candidate = currentUserService.requireActiveCandidateUser(jwt);
    var vacancy = loadPublishedVacancy(vacancyId);
    var contacts = requireContacts(request);
    if (vacancyApplicationRepository.existsByVacancyIdAndCandidateUserId(vacancyId, candidate.id())) {
      throw new DuplicateApplicationException();
    }
    var application =
        vacancyApplicationRepository.save(
            VacancyApplication.builder()
                .vacancy(vacancy)
                .candidateUserId(candidate.id())
                .candidateFirstName(candidate.firstName())
                .candidateLastName(candidate.lastName())
                .candidateEmail(candidate.email())
                .contactEmail(contacts.email())
                .contactPhone(contacts.phone())
                .contactTelegram(contacts.telegram())
                .contactLinkedIn(contacts.linkedIn())
                .contactPortfolioUrl(contacts.portfolioUrl())
                .contactHhResumeUrl(contacts.hhResumeUrl())
                .coverLetter(trimToNull(request.coverLetter()))
                .status(VacancyApplicationStatus.INTERVIEW_CREATED)
                .build());

    var interview = aiInterviewClient.createSession(toInterviewRequest(vacancy, application));
    application.setInterviewSessionId(interview.sessionId());
    return toResponse(application);
  }

  @Override
  @Transactional(readOnly = true)
  public List<VacancyApplicationResponse> getMyApplications(Jwt jwt) {
    var candidate = currentUserService.requireActiveCandidateUser(jwt);
    return vacancyApplicationRepository.findByCandidateUserIdOrderByCreatedAtDesc(candidate.id()).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public VacancyApplicationResponse getMyApplication(Jwt jwt, UUID applicationId) {
    var candidate = currentUserService.requireActiveCandidateUser(jwt);
    return vacancyApplicationRepository
        .findByIdAndCandidateUserId(applicationId, candidate.id())
        .map(this::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public List<EmployerVacancyApplicationResponse> getVacancyApplications(Jwt jwt, UUID vacancyId) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    var vacancy =
        vacancyRepository
            .findById(vacancyId)
            .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));
    organizationAccessService.requireWritableMember(vacancy.getOrganization().getId(), user.id());
    return vacancyApplicationRepository.findByVacancyIdOrderByCreatedAtDesc(vacancyId).stream()
        .map(this::toEmployerResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public EmployerApplicationReportDto getEmployerReport(Jwt jwt, UUID vacancyId, UUID applicationId) {
    var user = currentUserService.requireActiveBusinessUser(jwt);
    var application = loadApplication(vacancyId, applicationId);
    organizationAccessService.requireWritableMember(application.getVacancy().getOrganization().getId(), user.id());
    var report = aiInterviewClient.getReport(application.getInterviewSessionId());
    return new EmployerApplicationReportDto(
        toCandidate(application),
        effectiveStatus(application, report),
        report.sessionConfidence(),
        recommendation(report.sessionConfidence()),
        report.topics(),
        report.questions(),
        application.getCreatedAt(),
        report.finishedAt());
  }

  @Override
  @Transactional(readOnly = true)
  public String exportVacancyApplicationsCsv(Jwt jwt, UUID vacancyId, String language) {
    var applications = getVacancyApplications(jwt, vacancyId);
    var reportLanguage = ReportLanguage.from(language);
    var csv = new StringBuilder();
    csv.append('\ufeff');
    appendCsvRow(
        csv,
        List.of(
            label(reportLanguage, "Candidate name", "Имя кандидата"),
            label(reportLanguage, "Email", "Email"),
            label(reportLanguage, "Phone", "Телефон"),
            "Telegram",
            "LinkedIn",
            label(reportLanguage, "Portfolio", "Портфолио"),
            "HH",
            label(reportLanguage, "Status", "Статус"),
            label(reportLanguage, "Recommendation", "Рекомендация"),
            label(reportLanguage, "Overall score", "Итоговая оценка"),
            label(reportLanguage, "Cover letter", "Сопроводительное письмо"),
            label(reportLanguage, "Applied at", "Дата отклика"),
            label(reportLanguage, "Completed at", "Дата завершения")));
    for (var application : applications) {
      var contacts = application.candidateContacts();
      appendCsvRow(
          csv,
          List.of(
              value(application.candidateName()),
              contacts != null ? value(contacts.email()) : "",
              contacts != null ? value(contacts.phone()) : "",
              contacts != null ? value(contacts.telegram()) : "",
              contacts != null ? value(contacts.linkedIn()) : "",
              contacts != null ? value(contacts.portfolioUrl()) : "",
              contacts != null ? value(contacts.hhResumeUrl()) : "",
              statusCsv(application.status(), reportLanguage),
              recommendationCsv(application.recommendation(), reportLanguage),
              percentText(application.sessionConfidence()),
              value(application.coverLetter()),
              csvDate(application.createdAt(), reportLanguage),
              csvDate(application.completedAt(), reportLanguage)));
    }
    return csv.toString();
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] exportEmployerReportPdf(Jwt jwt, UUID vacancyId, UUID applicationId, String language) {
    var report = getEmployerReport(jwt, vacancyId, applicationId);
    var output = new ByteArrayOutputStream();
    var document = new Document();
    PdfWriter.getInstance(document, output);
    document.open();
    addReportContent(document, report, ReportLanguage.from(language));
    document.close();
    return output.toByteArray();
  }

  private VacancyApplication loadApplication(UUID vacancyId, UUID applicationId) {
    var application =
        vacancyApplicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    if (!application.getVacancy().getId().equals(vacancyId)) {
      throw new ResourceNotFoundException("Application not found");
    }
    return application;
  }

  private Vacancy loadPublishedVacancy(UUID vacancyId) {
    var vacancy =
        vacancyRepository
            .findById(vacancyId)
            .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));
    if (vacancy.getStatus() != VacancyStatus.PUBLISHED) {
      throw new ResourceNotFoundException("Vacancy not found");
    }
    return vacancy;
  }

  private AiStartInterviewRequest toInterviewRequest(Vacancy vacancy, VacancyApplication application) {
    var technologies = vacancyTechnologyRepository.findTechnologyKeysByVacancyId(vacancy.getId());
    var customQuestions =
        vacancyQuestionRepository.findByVacancyIdAndActiveTrueOrderByDisplayOrderAsc(vacancy.getId()).stream()
            .map(
                q ->
                    new AiCustomQuestionRequest(
                        q.getId(),
                        q.getQuestionText(),
                        q.getExpectedAnswer(),
                        q.getEvaluationRubric(),
                        q.getTopic(),
                        q.getDisplayOrder()))
            .toList();
    var minPrimaryQuestions = Math.max(vacancy.getMinPrimaryQuestions(), customQuestions.size());
    var maxPrimaryQuestions = Math.max(vacancy.getMaxPrimaryQuestions(), minPrimaryQuestions);
    return new AiStartInterviewRequest(
        application.getCandidateUserId(),
        vacancy.getId(),
        application.getId(),
        minPrimaryQuestions,
        maxPrimaryQuestions,
        vacancy.getMaxFollowUpsPerPrimary(),
        vacancy.getLevel().name(),
        "Russian",
        technologies,
        customQuestions);
  }

  private VacancyApplicationResponse toResponse(VacancyApplication application) {
    var report = safeReport(application.getInterviewSessionId());
    return new VacancyApplicationResponse(
        application.getId(),
        application.getVacancy().getId(),
        application.getCandidateUserId(),
        candidateName(application),
        toContacts(application),
        effectiveStatus(application, report),
        application.getInterviewSessionId(),
        report != null ? report.sessionConfidence() : null,
        report != null ? recommendation(report.sessionConfidence()) : null,
        application.getCoverLetter(),
        application.getCreatedAt(),
        report != null ? report.finishedAt() : null,
        application.getUpdatedAt());
  }

  private EmployerVacancyApplicationResponse toEmployerResponse(VacancyApplication application) {
    var report = safeReport(application.getInterviewSessionId());
    return new EmployerVacancyApplicationResponse(
        application.getId(),
        candidateName(application),
        toContacts(application),
        effectiveStatus(application, report),
        report != null ? report.sessionConfidence() : null,
        report != null ? recommendation(report.sessionConfidence()) : null,
        application.getCoverLetter(),
        application.getCreatedAt(),
        report != null ? report.finishedAt() : null,
        application.getUpdatedAt());
  }

  private AiInterviewReportDto safeReport(UUID interviewSessionId) {
    if (interviewSessionId == null) {
      return null;
    }
    try {
      return aiInterviewClient.getReport(interviewSessionId);
    } catch (RestClientException ex) {
      return null;
    }
  }

  private VacancyApplicationStatus effectiveStatus(
      VacancyApplication application, AiInterviewReportDto report) {
    if (report != null && "COMPLETED".equals(report.status())) {
      return VacancyApplicationStatus.INTERVIEW_COMPLETED;
    }
    if (report != null && "IN_PROGRESS".equals(report.status())) {
      return VacancyApplicationStatus.INTERVIEW_IN_PROGRESS;
    }
    return application.getStatus();
  }

  private String recommendation(Double sessionConfidence) {
    if (sessionConfidence == null) {
      return null;
    }
    if (sessionConfidence >= 0.90) return "STRONG_YES";
    if (sessionConfidence >= 0.75) return "YES";
    if (sessionConfidence >= 0.60) return "MAYBE";
    if (sessionConfidence >= 0.40) return "NO";
    return "STRONG_NO";
  }

  private EmployerCandidateDto toCandidate(VacancyApplication application) {
    return new EmployerCandidateDto(
        application.getCandidateFirstName(),
        application.getCandidateLastName(),
        toContacts(application));
  }

  private CandidateContactsDto requireContacts(VacancyApplyRequest request) {
    if (request == null || request.candidateContacts() == null) {
      throw new InvalidApplicationRequestException("At least one candidate contact method is required");
    }
    var contacts =
        new CandidateContactsDto(
            trimToNull(request.candidateContacts().email()),
            trimToNull(request.candidateContacts().phone()),
            trimToNull(request.candidateContacts().telegram()),
            trimToNull(request.candidateContacts().linkedIn()),
            trimToNull(request.candidateContacts().portfolioUrl()),
            trimToNull(request.candidateContacts().hhResumeUrl()));
    if (contacts.email() == null
        && contacts.phone() == null
        && contacts.telegram() == null
        && contacts.linkedIn() == null
        && contacts.portfolioUrl() == null
        && contacts.hhResumeUrl() == null) {
      throw new InvalidApplicationRequestException("At least one candidate contact method is required");
    }
    return contacts;
  }

  private CandidateContactsDto toContacts(VacancyApplication application) {
    return new CandidateContactsDto(
        application.getContactEmail(),
        application.getContactPhone(),
        application.getContactTelegram(),
        application.getContactLinkedIn(),
        application.getContactPortfolioUrl(),
        application.getContactHhResumeUrl());
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    var trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String candidateName(VacancyApplication application) {
    var firstName = application.getCandidateFirstName();
    var lastName = application.getCandidateLastName();
    var name = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    return name.isBlank() ? null : name;
  }

  private void addReportContent(Document document, EmployerApplicationReportDto report, ReportLanguage language) {
    var fonts = PdfFonts.load();

    document.add(new Paragraph(label(language, "Candidate Interview Report", "Отчет по техническому интервью"), fonts.title()));
    document.add(new Paragraph(label(language, "Generated at: ", "Сформировано: ") + formatDate(Instant.now(), language), fonts.normal()));
    document.add(new Paragraph(" "));

    document.add(new Paragraph(label(language, "Candidate", "Кандидат"), fonts.section()));
    var candidate = report.candidate();
    document.add(new Paragraph(label(language, "Name: ", "Имя: ") + empty(fullName(candidate), language), fonts.normal()));
    if (candidate != null && candidate.contacts() != null) {
      document.add(new Paragraph(label(language, "Contact email: ", "Контактный email: ") + empty(candidate.contacts().email(), language), fonts.normal()));
      document.add(new Paragraph(label(language, "Phone: ", "Телефон: ") + empty(candidate.contacts().phone(), language), fonts.normal()));
      document.add(new Paragraph("Telegram: " + empty(candidate.contacts().telegram(), language), fonts.normal()));
      document.add(new Paragraph("LinkedIn: " + empty(candidate.contacts().linkedIn(), language), fonts.normal()));
      document.add(new Paragraph(label(language, "Portfolio: ", "Портфолио: ") + empty(candidate.contacts().portfolioUrl(), language), fonts.normal()));
      document.add(new Paragraph("HH: " + empty(candidate.contacts().hhResumeUrl(), language), fonts.normal()));
    }
    document.add(new Paragraph(" "));

    document.add(new Paragraph(label(language, "Summary", "Итоги"), fonts.section()));
    document.add(new Paragraph(label(language, "Status: ", "Статус: ") + statusLabel(report.status(), language), fonts.normal()));
    document.add(new Paragraph(label(language, "Recommendation: ", "Рекомендация: ") + recommendationLabel(report.recommendation(), language), fonts.normal()));
    document.add(new Paragraph(label(language, "Overall score: ", "Итоговая оценка: ") + percent(report.sessionConfidence()), fonts.normal()));
    document.add(new Paragraph(label(language, "Applied at: ", "Дата отклика: ") + formatDate(report.createdAt(), language), fonts.normal()));
    document.add(new Paragraph(label(language, "Completed at: ", "Дата завершения: ") + formatDate(report.completedAt(), language), fonts.normal()));
    document.add(new Paragraph(" "));

    document.add(new Paragraph(label(language, "Topic Scores", "Оценка по темам"), fonts.section()));
    for (AiTopicStateSummaryDto topic : report.topics() != null ? report.topics() : List.<AiTopicStateSummaryDto>of()) {
      document.add(new Paragraph(topicLabel(topic.topic(), language), fonts.bold()));
      document.add(new Paragraph(label(language, "Questions: ", "Вопросов: ") + topic.questionsAsked(), fonts.normal()));
      document.add(new Paragraph(label(language, "Average score: ", "Средний результат: ") + percent(topic.avgScore()), fonts.normal()));
      document.add(new Paragraph(label(language, "Mastery: ", "Уровень владения: ") + percent(topic.masteryScore()), fonts.normal()));
      document.add(new Paragraph(label(language, "Evaluation confidence: ", "Уверенность оценки: ") + percent(topic.confidenceScore()), fonts.normal()));
      document.add(new Paragraph(" "));
    }

    document.add(new Paragraph(label(language, "Questions", "Вопросы и ответы"), fonts.section()));
    for (AiInterviewQuestionReportDto question : report.questions() != null ? report.questions() : List.<AiInterviewQuestionReportDto>of()) {
      document.add(new Paragraph(label(language, "Question ", "Вопрос ") + question.roundNumber() + " | " + topicLabel(question.topic(), language), fonts.section()));
      document.add(new Paragraph(label(language, "Type: ", "Тип: ") + questionTypeLabel(question.questionType(), language), fonts.muted()));
      document.add(new Paragraph(label(language, "Difficulty: ", "Сложность: ") + difficultyLabel(question.difficulty(), language), fonts.muted()));
      document.add(new Paragraph(label(language, "Question:", "Вопрос:"), fonts.bold()));
      document.add(new Paragraph(empty(question.questionText(), language), fonts.normal()));
      document.add(new Paragraph(label(language, "Candidate answer:", "Ответ кандидата:"), fonts.bold()));
      document.add(new Paragraph(empty(question.answerText(), language), fonts.normal()));
      document.add(new Paragraph(label(language, "Score: ", "Оценка: ") + percent(question.totalScore()), fonts.normal()));
      document.add(new Paragraph(label(language, "Feedback:", "Обратная связь:"), fonts.bold()));
      document.add(new Paragraph(empty(question.feedback(), language), fonts.normal()));
      document.add(new Paragraph(label(language, "Knowledge gaps: ", "Пробелы в знаниях: ") + gaps(question.knowledgeGaps(), language), fonts.normal()));
      document.add(new Paragraph(" "));
    }
  }

  private String label(ReportLanguage language, String en, String ru) {
    return language == ReportLanguage.RU ? ru : en;
  }

  private String empty(String value, ReportLanguage language) {
    return value == null || value.isBlank() ? label(language, "Not provided", "Не указано") : value;
  }

  private String gaps(List<String> gaps, ReportLanguage language) {
    if (gaps == null || gaps.isEmpty()) {
      return label(language, "None identified", "Не выявлены");
    }
    return String.join(", ", gaps);
  }

  private String percent(Number value) {
    if (value == null) {
      return "-";
    }
    return Math.round(value.doubleValue() * 100.0) + "%";
  }

  private String percentText(Number value) {
    return value == null ? "" : percent(value);
  }

  private String formatDate(Instant instant, ReportLanguage language) {
    if (instant == null) {
      return "-";
    }
    var zone = ZoneId.systemDefault();
    var pattern = language == ReportLanguage.RU ? "dd.MM.yyyy HH:mm" : "yyyy-MM-dd HH:mm";
    return DateTimeFormatter.ofPattern(pattern).withZone(zone).format(instant);
  }

  private String csvDate(Instant instant, ReportLanguage language) {
    return instant == null ? "" : formatDate(instant, language);
  }

  private String statusCsv(VacancyApplicationStatus status, ReportLanguage language) {
    return status == null ? "" : statusLabel(status, language);
  }

  private String recommendationCsv(String recommendation, ReportLanguage language) {
    return recommendation == null || recommendation.isBlank()
        ? ""
        : recommendationLabel(recommendation, language);
  }

  private String statusLabel(VacancyApplicationStatus status, ReportLanguage language) {
    if (status == null) {
      return "-";
    }
    return switch (status) {
      case INTERVIEW_COMPLETED -> label(language, "Interview completed", "Интервью завершено");
      case INTERVIEW_IN_PROGRESS -> label(language, "Interview in progress", "Интервью в процессе");
      case INTERVIEW_CREATED -> label(language, "Interview created", "Интервью создано");
      case REJECTED -> label(language, "Rejected", "Отклонено");
      case WITHDRAWN -> label(language, "Withdrawn", "Отозвано");
    };
  }

  private String recommendationLabel(String recommendation, ReportLanguage language) {
    if (recommendation == null || recommendation.isBlank()) {
      return "-";
    }
    return switch (recommendation) {
      case "STRONG_YES" -> label(language, "Strong yes", "Сильная рекомендация");
      case "YES" -> label(language, "Yes", "Рекомендуется");
      case "MAYBE" -> label(language, "Maybe", "Можно рассмотреть");
      case "NO" -> label(language, "No", "Скорее не рекомендуется");
      case "STRONG_NO" -> label(language, "Strong no", "Не рекомендуется");
      default -> recommendation;
    };
  }

  private String topicLabel(String topic, ReportLanguage language) {
    if (topic == null || topic.isBlank()) {
      return "-";
    }
    return switch (topic) {
      case "custom" -> label(language, "Custom questions", "Пользовательские вопросы");
      case "language_basics" -> label(language, "Language basics", "Основы языка");
      case "concurrency" -> label(language, "Concurrency", "Многопоточность");
      case "data_access" -> label(language, "Data access", "Доступ к данным");
      case "database", "sql" -> label(language, "Databases", "Базы данных");
      case "spring", "spring_core" -> "Spring";
      case "spring_security", "security" -> label(language, "Security", "Безопасность");
      case "architecture" -> label(language, "Architecture", "Архитектура");
      case "testing" -> label(language, "Testing", "Тестирование");
      default -> humanize(topic);
    };
  }

  private String questionTypeLabel(String type, ReportLanguage language) {
    if (type == null || type.isBlank()) {
      return "-";
    }
    return switch (type) {
      case "PRIMARY" -> label(language, "Primary question", "Основной вопрос");
      case "FOLLOW_UP" -> label(language, "Follow-up", "Уточняющий вопрос");
      default -> humanize(type);
    };
  }

  private String difficultyLabel(String difficulty, ReportLanguage language) {
    if (difficulty == null || difficulty.isBlank()) {
      return "-";
    }
    return switch (difficulty) {
      case "EASY" -> label(language, "Easy", "Легкая");
      case "MEDIUM" -> label(language, "Medium", "Средняя");
      case "HARD" -> label(language, "Hard", "Сложная");
      default -> humanize(difficulty);
    };
  }

  private String humanize(String value) {
    var text = value.toLowerCase().replace('_', ' ').trim();
    return text.isBlank() ? "-" : Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }

  private String fullName(EmployerCandidateDto candidate) {
    if (candidate == null) {
      return "";
    }
    var name = ((candidate.firstName() != null ? candidate.firstName() : "")
            + " "
            + (candidate.lastName() != null ? candidate.lastName() : ""))
        .trim();
    return name.isBlank() ? "" : name;
  }

  private void appendCsvRow(StringBuilder csv, List<String> values) {
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        csv.append(',');
      }
      csv.append(csvEscape(values.get(i)));
    }
    csv.append('\n');
  }

  private String csvEscape(String value) {
    if (value == null) {
      return "";
    }
    return '"' + value.replace("\r", " ").replace("\n", " ").replace("\"", "\"\"") + '"';
  }

  private String value(Object value) {
    return value == null ? "" : value.toString();
  }

  private enum ReportLanguage {
    RU,
    EN;

    static ReportLanguage from(String language) {
      if (language == null || language.isBlank()) {
        return RU;
      }
      return language.toLowerCase().startsWith("en") ? EN : RU;
    }
  }

  private record PdfFonts(Font title, Font section, Font normal, Font bold, Font muted) {
    static PdfFonts load() {
      try {
        var regular = BaseFont.createFont(resolveFontPath(false), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        var bold = BaseFont.createFont(resolveFontPath(true), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        return new PdfFonts(
            new Font(bold, 18),
            new Font(bold, 13),
            new Font(regular, 10),
            new Font(bold, 10),
            new Font(regular, 8));
      } catch (Exception ex) {
        throw new IllegalStateException("Failed to load PDF Unicode font", ex);
      }
    }

    private static String resolveFontPath(boolean bold) throws IOException {
      var candidates =
          bold
              ? List.of(
                  "/usr/share/fonts/noto/NotoSans-Bold.ttf",
                  "/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf",
                  "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf")
              : List.of(
                  "/usr/share/fonts/noto/NotoSans-Regular.ttf",
                  "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf",
                  "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf");
      return candidates.stream()
          .map(Path::of)
          .filter(Files::exists)
          .findFirst()
          .map(Path::toString)
          .orElseThrow(() -> new IOException("No Unicode PDF font found"));
    }
  }
}
