package io.github.kstnnn.organization.service.repository;

import io.github.kstnnn.organization.service.model.VacancyApplication;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacancyApplicationRepository extends JpaRepository<VacancyApplication, UUID> {

  boolean existsByVacancyIdAndCandidateUserId(UUID vacancyId, UUID candidateUserId);

  Optional<VacancyApplication> findByIdAndCandidateUserId(UUID id, UUID candidateUserId);

  List<VacancyApplication> findByCandidateUserIdOrderByCreatedAtDesc(UUID candidateUserId);

  List<VacancyApplication> findByVacancyIdOrderByCreatedAtDesc(UUID vacancyId);
}
