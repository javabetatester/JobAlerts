package com.jobsearch.scheduler;

import com.jobsearch.dto.JSearchDTO;
import com.jobsearch.dto.JobAlertDTO;
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
import java.util.Collections;

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
    private final JobDuplicateService jobDuplicateService;

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
            if (alertResponse.getSearchQuery() == null || alertResponse.getSearchQuery().trim().isEmpty()) {
                log.warn("Alerta {} tem query de busca vazia, pulando...", alertResponse.getId());
                return;
            }

            if (alertResponse.getLocation() == null || alertResponse.getLocation().trim().isEmpty()) {
                log.warn("Alerta {} tem localização vazia, pulando...", alertResponse.getId());
                return;
            }

            log.debug("Buscando vagas para query: '{}' em: '{}'",
                    alertResponse.getSearchQuery(), alertResponse.getLocation());

            JSearchDTO.JobSearchResponse searchResponse = jSearchService.searchJobsWithFilters(
                    alertResponse.getSearchQuery().trim(),
                    alertResponse.getLocation().trim(),
                    null,
                    1
            );

            if (searchResponse == null) {
                log.warn("Resposta da API JSearch é null para alerta: {}", alertResponse.getId());
                jobAlertService.updateLastChecked(alertResponse.getId());
                return;
            }

            if (searchResponse.getData() == null || searchResponse.getData().isEmpty()) {
                log.info("Nenhuma vaga encontrada na API para alerta: {}", alertResponse.getTitle());
                jobAlertService.updateLastChecked(alertResponse.getId());
                return;
            }

            log.info("API retornou {} vagas para alerta: {}",
                    searchResponse.getData().size(), alertResponse.getTitle());

            JobAlert jobAlert = convertToJobAlert(alertResponse);
            if (jobAlert == null) {
                log.error("Erro ao converter JobAlertResponse para JobAlert - ID: {}", alertResponse.getId());
                return;
            }

            List<JobVacancy> matchedJobs = jobMatchingService.processAndMatchJobs(searchResponse, jobAlert);

            if (matchedJobs != null && !matchedJobs.isEmpty()) {
                log.info("Encontradas {} vagas correspondentes para alerta: {}",
                        matchedJobs.size(), alertResponse.getTitle());

                User user = getUserFromAlert(alertResponse);
                if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {

                    List<JobVacancy> newJobs = jobDuplicateService.filterAlreadySentJobs(user, matchedJobs);

                    if (!newJobs.isEmpty()) {
                        try {
                            emailService.sendJobAlertEmail(user, newJobs, alertResponse.getTitle());
                            jobDuplicateService.markJobsAsSent(user, newJobs, alertResponse.getTitle());
                            log.info("Email enviado com {} vagas novas para: {}", newJobs.size(), user.getEmail());
                        } catch (Exception emailError) {
                            log.error("Erro ao enviar email para {}: {}", user.getEmail(), emailError.getMessage());
                        }
                    } else {
                        log.info("Todas as vagas já foram enviadas anteriormente para usuário: {}", user.getEmail());
                    }
                } else {
                    log.warn("Usuário não encontrado ou email inválido para alerta: {}", alertResponse.getId());
                }
            } else {
                log.debug("Nenhuma vaga nova encontrada para alerta: {}", alertResponse.getTitle());
            }

            jobAlertService.updateLastChecked(alertResponse.getId());

        } catch (Exception e) {
            log.error("Erro detalhado ao processar alerta {}: {}", alertResponse.getId(), e.getMessage(), e);
            try {
                jobAlertService.updateLastChecked(alertResponse.getId());
            } catch (Exception updateError) {
                log.error("Erro ao atualizar lastChecked para alerta {}: {}",
                        alertResponse.getId(), updateError.getMessage());
            }
        }
    }

    private User getUserFromAlert(JobAlertDTO.JobAlertResponse alertResponse) {
        try {
            if (alertResponse == null || alertResponse.getId() == null) {
                log.error("AlertResponse ou ID é null");
                return null;
            }

            JobAlert fullAlert = jobAlertService.getJobAlertEntityById(alertResponse.getId());
            if (fullAlert == null) {
                log.error("JobAlert não encontrado para ID: {}", alertResponse.getId());
                return null;
            }

            User user = fullAlert.getUser();
            if (user == null) {
                log.error("Usuário é null para alerta ID: {}", alertResponse.getId());
                return null;
            }

            log.debug("Usuário encontrado: {} ({})", user.getName(), user.getEmail());
            return user;

        } catch (Exception e) {
            log.error("Erro ao buscar usuário do alerta {}: {}", alertResponse.getId(), e.getMessage(), e);
            return null;
        }
    }

    private JobAlert convertToJobAlert(JobAlertDTO.JobAlertResponse alertResponse) {
        try {
            if (alertResponse == null) {
                log.error("AlertResponse é null");
                return null;
            }

            JobAlert jobAlert = new JobAlert();
            jobAlert.setId(alertResponse.getId());
            jobAlert.setTitle(alertResponse.getTitle());
            jobAlert.setSearchQuery(alertResponse.getSearchQuery());
            jobAlert.setLocation(alertResponse.getLocation());
            jobAlert.setLocationType(alertResponse.getLocationType());
            jobAlert.setExperienceLevel(alertResponse.getExperienceLevel());
            jobAlert.setMinimumMatchingTags(alertResponse.getMinimumMatchingTags() != null ?
                    alertResponse.getMinimumMatchingTags() : 1);
            jobAlert.setIsActive(alertResponse.getIsActive() != null ? alertResponse.getIsActive() : true);
            jobAlert.setCreatedAt(alertResponse.getCreatedAt());
            jobAlert.setLastChecked(alertResponse.getLastChecked());

            if (alertResponse.getTags() != null && !alertResponse.getTags().isEmpty()) {
                try {
                    java.util.Set<com.jobsearch.entity.AlertTag> alertTags = alertResponse.getTags().stream()
                            .filter(tagResponse -> tagResponse != null && tagResponse.getTag() != null)
                            .map(tagResponse -> {
                                com.jobsearch.entity.AlertTag alertTag = new com.jobsearch.entity.AlertTag();
                                alertTag.setId(tagResponse.getId());
                                alertTag.setTag(tagResponse.getTag());
                                alertTag.setIsRequired(tagResponse.getIsRequired() != null ?
                                        tagResponse.getIsRequired() : false);
                                alertTag.setJobAlert(jobAlert);
                                return alertTag;
                            })
                            .collect(java.util.stream.Collectors.toSet());

                    jobAlert.setAlertTags(alertTags);
                    log.debug("Convertidas {} tags para alerta {}", alertTags.size(), alertResponse.getId());
                } catch (Exception tagError) {
                    log.error("Erro ao converter tags para alerta {}: {}",
                            alertResponse.getId(), tagError.getMessage());
                    jobAlert.setAlertTags(Collections.emptySet());
                }
            } else {
                log.debug("Alerta {} não possui tags", alertResponse.getId());
                jobAlert.setAlertTags(Collections.emptySet());
            }

            return jobAlert;

        } catch (Exception e) {
            log.error("Erro ao converter JobAlertResponse para JobAlert - ID: {}, Erro: {}",
                    alertResponse != null ? alertResponse.getId() : "null", e.getMessage(), e);
            return null;
        }
    }
}