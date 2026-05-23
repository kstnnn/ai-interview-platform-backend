package io.github.kstnnn.organization.service.repository;

import io.github.kstnnn.organization.service.model.Vacancy;
import io.github.kstnnn.organization.service.model.VacancyStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacancyRepository extends JpaRepository<Vacancy, UUID> {

  List<Vacancy> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

  List<Vacancy> findByStatusOrderByCreatedAtDesc(VacancyStatus status);
}
