package com.jobsearch.service;

import com.jobsearch.entity.JobVacancy;
import com.jobsearch.entity.User;
import com.jobsearch.entity.UserJobHistory;
import com.jobsearch.repository.UserJobHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobDuplicateService {

    private final UserJobHistoryRepository userJobHistoryRepository;

    public List<JobVacancy> filterAlreadySentJobs(User user, List<JobVacancy> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return jobs;
        }

        List<JobVacancy> newJobs = jobs.stream()
                .filter(job -> !wasJobSentToUser(user.getId(), job.getId()))
                .collect(Collectors.toList());

        log.info("Filtradas {} vagas já enviadas. {} vagas novas para enviar para usuário {}",
                jobs.size() - newJobs.size(), newJobs.size(), user.getId());

        return newJobs;
    }

    @Transactional
    public void markJobsAsSent(User user, List<JobVacancy> jobs, String alertTitle) {
        for (JobVacancy job : jobs) {
            UserJobHistory history = new UserJobHistory();
            history.setUser(user);
            history.setJobVacancy(job);
            history.setAlertTitle(alertTitle);

            try {
                userJobHistoryRepository.save(history);
            } catch (Exception e) {
                log.warn("Erro ao salvar histórico para vaga {} e usuário {}: {}",
                        job.getId(), user.getId(), e.getMessage());
            }
        }

        log.info("Marcadas {} vagas como enviadas para usuário {}", jobs.size(), user.getId());
    }

    private boolean wasJobSentToUser(Long userId, Long jobVacancyId) {
        return userJobHistoryRepository.existsByUserIdAndJobVacancyId(userId, jobVacancyId);
    }

    @Transactional
    public void cleanOldHistory(Long userId, int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        try {
            userJobHistoryRepository.deleteByUserIdAndSentAtBefore(userId, cutoffDate);
            log.info("Limpeza de histórico realizada para usuário {}, removendo registros anteriores a {}",
                    userId, cutoffDate);
        } catch (Exception e) {
            log.error("Erro ao limpar histórico antigo para usuário {}: {}", userId, e.getMessage());
        }
    }

    public List<UserJobHistory> getUserJobHistory(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userJobHistoryRepository.findByUserIdAndSentAtAfter(userId, since);
    }
}