package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.Question;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
  List<Question> findAllByExternalIdIn(List<String> externalIds);

  List<Question> findAllByExternalIdInAndActiveTrue(List<String> externalIds);

  Optional<Question> findByExternalId(String externalId);

  @Query("SELECT q FROM Question q JOIN FETCH q.technology WHERE q.active = true")
  List<Question> findAllWithTechnology();

  @Query(
      value =
          "SELECT q FROM Question q JOIN FETCH q.technology t "
              + "WHERE (:technologyKey IS NULL OR t.key = :technologyKey) "
              + "AND (:difficulty IS NULL OR q.difficulty = :difficulty) "
              + "AND (:active IS NULL OR q.active = :active)",
      countQuery =
          "SELECT COUNT(q) FROM Question q JOIN q.technology t "
              + "WHERE (:technologyKey IS NULL OR t.key = :technologyKey) "
              + "AND (:difficulty IS NULL OR q.difficulty = :difficulty) "
              + "AND (:active IS NULL OR q.active = :active)")
  Page<Question> findAdminQuestions(
      @Param("technologyKey") String technologyKey,
      @Param("difficulty") io.github.kstnnn.ai.interview.service.model.Difficulty difficulty,
      @Param("active") Boolean active,
      Pageable pageable);

  @Query(
      value =
          "SELECT q FROM Question q JOIN FETCH q.technology t "
              + "WHERE (LOWER(q.externalId) LIKE CONCAT('%', :search, '%') "
              + "OR LOWER(q.topic) LIKE CONCAT('%', :search, '%') "
              + "OR LOWER(COALESCE(q.subtopic, '')) LIKE CONCAT('%', :search, '%') "
              + "OR LOWER(q.questionText) LIKE CONCAT('%', :search, '%')) "
              + "AND (:technologyKey IS NULL OR t.key = :technologyKey) "
              + "AND (:difficulty IS NULL OR q.difficulty = :difficulty) "
              + "AND (:active IS NULL OR q.active = :active)",
      countQuery =
          "SELECT COUNT(q) FROM Question q JOIN q.technology t "
              + "WHERE (LOWER(q.externalId) LIKE CONCAT('%', :search, '%') "
              + "OR LOWER(q.topic) LIKE CONCAT('%', :search, '%') "
              + "OR LOWER(COALESCE(q.subtopic, '')) LIKE CONCAT('%', :search, '%') "
              + "OR LOWER(q.questionText) LIKE CONCAT('%', :search, '%')) "
              + "AND (:technologyKey IS NULL OR t.key = :technologyKey) "
              + "AND (:difficulty IS NULL OR q.difficulty = :difficulty) "
              + "AND (:active IS NULL OR q.active = :active)")
  Page<Question> findAdminQuestionsBySearch(
      @Param("search") String search,
      @Param("technologyKey") String technologyKey,
      @Param("difficulty") io.github.kstnnn.ai.interview.service.model.Difficulty difficulty,
      @Param("active") Boolean active,
      Pageable pageable);

  @Query(
      """
      SELECT q FROM Question q JOIN FETCH q.technology t
      WHERE (:technologyKey IS NULL OR t.key = :technologyKey)
      AND (:difficulty IS NULL OR q.difficulty = :difficulty)
      AND (:active IS NULL OR q.active = :active)
      ORDER BY t.key, q.topic, q.subtopic, q.difficulty, q.externalId
      """)
  List<Question> findAdminQuestionsForExport(
      @Param("technologyKey") String technologyKey,
      @Param("difficulty") io.github.kstnnn.ai.interview.service.model.Difficulty difficulty,
      @Param("active") Boolean active);

  @Query(
      """
      SELECT q FROM Question q JOIN FETCH q.technology t
      WHERE (LOWER(q.externalId) LIKE CONCAT('%', :search, '%')
      OR LOWER(q.topic) LIKE CONCAT('%', :search, '%')
      OR LOWER(COALESCE(q.subtopic, '')) LIKE CONCAT('%', :search, '%')
      OR LOWER(q.questionText) LIKE CONCAT('%', :search, '%'))
      AND (:technologyKey IS NULL OR t.key = :technologyKey)
      AND (:difficulty IS NULL OR q.difficulty = :difficulty)
      AND (:active IS NULL OR q.active = :active)
      ORDER BY t.key, q.topic, q.subtopic, q.difficulty, q.externalId
      """)
  List<Question> findAdminQuestionsBySearchForExport(
      @Param("search") String search,
      @Param("technologyKey") String technologyKey,
      @Param("difficulty") io.github.kstnnn.ai.interview.service.model.Difficulty difficulty,
      @Param("active") Boolean active);
}
