package com.jobsearch.repository;

import com.jobsearch.entity.UserJobHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserJobHistoryRepository extends JpaRepository<UserJobHistory, Long> {

    boolean existsByUserIdAndJobVacancyId(Long userId, Long jobVacancyId);

    List<UserJobHistory> findByUserIdAndSentAtAfter(Long userId, LocalDateTime since);

    void deleteByUserIdAndSentAtBefore(Long userId, LocalDateTime before);
}