package io.github.kstnnn.organization.service.repository;

import io.github.kstnnn.organization.service.model.VacancyTechnology;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

public interface VacancyTechnologyRepository extends ListCrudRepository<VacancyTechnology, Long> {

  @Query(
      """
      select vt.technologyKey
      from VacancyTechnology vt
      where vt.vacancy.id = :vacancyId
      order by vt.technologyKey
      """)
  List<String> findTechnologyKeysByVacancyId(@Param("vacancyId") UUID vacancyId);

  @Modifying
  @Query("delete from VacancyTechnology vt where vt.vacancy.id = :vacancyId")
  void deleteByVacancyId(@Param("vacancyId") UUID vacancyId);
}
