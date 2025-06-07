package com.jobsearch.repository;

import com.jobsearch.entity.JobVacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobVacancyRepository extends JpaRepository<JobVacancy, Long> {

    Optional<JobVacancy> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);

    List<JobVacancy> findByCreatedAtAfter(LocalDateTime dateTime);
}