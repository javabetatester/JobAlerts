package com.jobsearch.scheduler;

import com.jobsearch.dto.JSearchDTO;
import com.jobsearch.dto.JobAlertDTO;
import com.jobsearch.dto.UserDTO;
import com.jobsearch.entity.JobAlert;
import com.jobsearch.entity.JobVacancy;
import com.jobsearch.entity.User;
import com.jobsearch.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "job.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class JobSearchScheduler {

    private final JobAlertService jobAlertService;
    private final UserService userService;
    private final JSearchService jSearchService;
    private final JobMatchingService jobMatchingService;
    private final EmailService emailService;

    @Scheduled(fixedRateString = "${job.scheduler.fixed-rate:3600000}")
    public void searchJobsForAllAlerts() {
        log.info("Iniciando busca automática de empregos...");

        try {
            List<JobAlertDTO.JobAlertResponse> activeAlerts = jobAlertService.getAllActiveAlerts();
            log.info("Encontrados {} alertas ativos para processar", activeAlerts.size());

            for (JobAlertDTO.JobAlertResponse alertResponse : activeAlerts) {
                try {
                    processJobAlert(alertResponse);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("Erro ao processar alerta {}: {}", alertResponse.getId(), e.getMessage(), e);
                }
            }

            log.info("Busca automática concluída");

        } catch (Exception e) {
            log.error("Erro na busca automática de empregos: {}", e.getMessage(), e);
        }
    }

    private void processJobAlert(JobAlertDTO.JobAlertResponse alertResponse) {
        log.debug("Processando alerta: {} - {}", alertResponse.getId(), alertResponse.getTitle());

        try {
            JobAlert jobAlert = convertToJobAlert(alertResponse);

            JSearchDTO.JobSearchResponse searchResponse = jSearchService.searchJobsWithFilters(
                    alertResponse.getSearchQuery(),
                    alertResponse.getLocation(),
                    null,
                    1
            );

            List<JobVacancy> matchedJobs = jobMatchingService.processAndMatchJobs(searchResponse, jobAlert);

            if (!matchedJobs.isEmpty()) {
                log.info("Encontradas {} vagas para alerta: {}", matchedJobs.size(), alertResponse.getTitle());

                User user = getUserFromAlert(alertResponse);
                if (user != null) {
                    emailService.sendJobAlertEmail(user, matchedJobs, alertResponse.getTitle());
                    log.info("Email enviado para: {}", user.getEmail());
                } else {
                    log.warn("Usuário não encontrado para alerta: {}", alertResponse.getId());
                }

                jobAlertService.updateLastChecked(alertResponse.getId());
            } else {
                log.debug("Nenhuma vaga nova encontrada para alerta: {}", alertResponse.getTitle());
                jobAlertService.updateLastChecked(alertResponse.getId());
            }

        } catch (Exception e) {
            log.error("Erro ao buscar vagas para alerta {}: {}", alertResponse.getId(), e.getMessage());
        }
    }

    private User getUserFromAlert(JobAlertDTO.JobAlertResponse alertResponse) {
        try {
            JobAlert fullAlert = jobAlertService.getJobAlertEntityById(alertResponse.getId());
            User user = fullAlert.getUser();
            return user;
        } catch (Exception e) {
            log.error("Erro ao buscar usuário do alerta {}: {}", alertResponse.getId(), e.getMessage());
            return null;
        }
    }

    private JobAlert convertToJobAlert(JobAlertDTO.JobAlertResponse alertResponse) {
        JobAlert jobAlert = new JobAlert();
        jobAlert.setId(alertResponse.getId());
        jobAlert.setTitle(alertResponse.getTitle());
        jobAlert.setSearchQuery(alertResponse.getSearchQuery());
        jobAlert.setLocation(alertResponse.getLocation());
        jobAlert.setLocationType(alertResponse.getLocationType());
        jobAlert.setExperienceLevel(alertResponse.getExperienceLevel());
        jobAlert.setMinimumMatchingTags(alertResponse.getMinimumMatchingTags());
        jobAlert.setIsActive(alertResponse.getIsActive());
        jobAlert.setCreatedAt(alertResponse.getCreatedAt());
        jobAlert.setLastChecked(alertResponse.getLastChecked());

        if (alertResponse.getTags() != null) {
            jobAlert.setAlertTags(alertResponse.getTags().stream()
                    .map(tagResponse -> {
                        com.jobsearch.entity.AlertTag alertTag = new com.jobsearch.entity.AlertTag();
                        alertTag.setId(tagResponse.getId());
                        alertTag.setTag(tagResponse.getTag());
                        alertTag.setIsRequired(tagResponse.getIsRequired());
                        alertTag.setJobAlert(jobAlert);
                        return alertTag;
                    })
                    .collect(java.util.Set::of, java.util.Set::add, java.util.Set::addAll));
        }

        return jobAlert;
    }
}