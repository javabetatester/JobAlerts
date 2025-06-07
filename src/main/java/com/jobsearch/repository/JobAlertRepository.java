package com.jobsearch.repository;

import com.jobsearch.entity.JobAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobAlertRepository extends JpaRepository<JobAlert, Long> {

    List<JobAlert> findByUserIdAndIsActiveTrue(Long userId);

    List<JobAlert> findByIsActiveTrue();

    @Query("SELECT ja FROM JobAlert ja WHERE ja.user.id = :userId AND ja.isActive = true")
    List<JobAlert> findActiveAlertsByUserId(Long userId);
}