package com.jobsearch.repository;

import com.jobsearch.entity.AlertTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertTagRepository extends JpaRepository<AlertTag, Long> {

    List<AlertTag> findByJobAlertId(Long jobAlertId);

    List<AlertTag> findByJobAlertIdAndIsRequiredTrue(Long jobAlertId);

    void deleteByJobAlertId(Long jobAlertId);
}