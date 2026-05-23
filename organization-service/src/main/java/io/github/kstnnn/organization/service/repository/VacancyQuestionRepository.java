package io.github.kstnnn.organization.service.repository;

import io.github.kstnnn.organization.service.model.VacancyQuestion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacancyQuestionRepository extends JpaRepository<VacancyQuestion, UUID> {

  List<VacancyQuestion> findByVacancyIdAndActiveTrueOrderByDisplayOrderAsc(UUID vacancyId);
}
