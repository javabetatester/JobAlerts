package com.jobsearch.service;

import com.jobsearch.dto.JSearchDTO;
import com.jobsearch.entity.AlertTag;
import com.jobsearch.entity.JobAlert;
import com.jobsearch.entity.JobVacancy;
import com.jobsearch.repository.JobVacancyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobMatchingService {

    private final JobVacancyRepository jobVacancyRepository;

    @Transactional
    public List<JobVacancy> processAndMatchJobs(JSearchDTO.JobSearchResponse searchResponse, JobAlert jobAlert) {
        List<JobVacancy> matchedJobs = new ArrayList<>();

        if (searchResponse == null || searchResponse.getData() == null || searchResponse.getData().isEmpty()) {
            log.info("Nenhuma vaga encontrada na busca para o alerta: {}",
                    jobAlert != null ? jobAlert.getTitle() : "null");
            return matchedJobs;
        }

        if (jobAlert == null) {
            log.error("JobAlert é null");
            return matchedJobs;
        }

        for (JSearchDTO.JobData jobData : searchResponse.getData()) {
            try {
                if (jobData == null || jobData.getJobId() == null || jobData.getJobId().trim().isEmpty()) {
                    log.warn("JobData ou JobId é null/vazio, pulando...");
                    continue;
                }

                JobVacancy vacancy = saveOrUpdateJobVacancy(jobData);
                if (vacancy == null) {
                    log.warn("Não foi possível salvar vaga com ID: {}", jobData.getJobId());
                    continue;
                }

                if (isJobMatching(vacancy, jobAlert)) {
                    matchedJobs.add(vacancy);
                    log.info("Vaga matched: {} - {}", vacancy.getTitle(), vacancy.getCompany());
                }

            } catch (Exception e) {
                log.error("Erro ao processar vaga: {}",
                        jobData != null ? jobData.getJobTitle() : "null", e);
            }
        }

        log.info("Processadas {} vagas, {} matches para alerta: {}",
                searchResponse.getData().size(), matchedJobs.size(), jobAlert.getTitle());

        return matchedJobs;
    }

    private JobVacancy saveOrUpdateJobVacancy(JSearchDTO.JobData jobData) {
        try {
            if (jobData == null || jobData.getJobId() == null) {
                log.warn("JobData ou JobId é null");
                return null;
            }

            JobVacancy vacancy = jobVacancyRepository.findByExternalId(jobData.getJobId())
                    .orElse(new JobVacancy());

            vacancy.setExternalId(jobData.getJobId());
            vacancy.setTitle(jobData.getJobTitle() != null ? jobData.getJobTitle() : "Título não informado");
            vacancy.setCompany(jobData.getEmployerName() != null ? jobData.getEmployerName() : "Empresa não informada");
            vacancy.setLocation(buildLocation(jobData));
            vacancy.setDescription(jobData.getJobDescription());
            vacancy.setJobUrl(jobData.getJobApplyLink());
            vacancy.setSalaryMin(jobData.getJobMinSalary());
            vacancy.setSalaryMax(jobData.getJobMaxSalary());
            vacancy.setEmploymentType(jobData.getJobEmploymentType());

            if (jobData.getJobPostedAtDatetimeUtc() != null && !jobData.getJobPostedAtDatetimeUtc().trim().isEmpty()) {
                try {
                    String dateStr = jobData.getJobPostedAtDatetimeUtc().replace("Z", "");
                    vacancy.setPublishedAt(LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } catch (Exception e) {
                    log.warn("Erro ao parsear data de publicação: {}", jobData.getJobPostedAtDatetimeUtc());
                    vacancy.setPublishedAt(LocalDateTime.now());
                }
            } else {
                vacancy.setPublishedAt(LocalDateTime.now());
            }

            return jobVacancyRepository.save(vacancy);

        } catch (Exception e) {
            log.error("Erro ao salvar/atualizar vaga: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean isJobMatching(JobVacancy vacancy, JobAlert jobAlert) {
        if (vacancy == null || jobAlert == null) {
            log.warn("Vacancy ou JobAlert é null");
            return false;
        }

        Set<AlertTag> alertTags = jobAlert.getAlertTags();
        if (alertTags == null || alertTags.isEmpty()) {
            log.debug("JobAlert {} não possui tags para matching", jobAlert.getId());
            return false;
        }

        String jobContent = buildJobContent(vacancy);
        if (jobContent == null || jobContent.trim().isEmpty()) {
            log.warn("Conteúdo da vaga está vazio para ID: {}", vacancy.getId());
            return false;
        }

        jobContent = jobContent.toLowerCase();

        List<AlertTag> requiredTags = alertTags.stream()
                .filter(tag -> tag != null && tag.getIsRequired() != null && tag.getIsRequired())
                .collect(Collectors.toList());

        for (AlertTag requiredTag : requiredTags) {
            if (requiredTag.getTag() == null || requiredTag.getTag().trim().isEmpty()) {
                log.warn("Tag obrigatória é null ou vazia");
                continue;
            }

            if (!jobContent.contains(requiredTag.getTag().toLowerCase())) {
                log.debug("Vaga {} não possui tag obrigatória: {}", vacancy.getTitle(), requiredTag.getTag());
                return false;
            }
        }

        String finalJobContent = jobContent;
        long matchingTagsCount = alertTags.stream()
                .filter(tag -> tag != null && tag.getTag() != null && !tag.getTag().trim().isEmpty())
                .filter(tag -> finalJobContent.contains(tag.getTag().toLowerCase()))
                .count();

        Integer minimumTags = jobAlert.getMinimumMatchingTags();
        if (minimumTags == null || minimumTags <= 0) {
            minimumTags = 1;
        }

        boolean hasMinimumTags = matchingTagsCount >= minimumTags;

        if (hasMinimumTags) {
            log.debug("Vaga {} matched com {} tags (mínimo: {})",
                    vacancy.getTitle(), matchingTagsCount, minimumTags);
        } else {
            log.debug("Vaga {} não atingiu mínimo de tags: {} de {} necessárias",
                    vacancy.getTitle(), matchingTagsCount, minimumTags);
        }

        return hasMinimumTags;
    }

    private String buildJobContent(JobVacancy vacancy) {
        if (vacancy == null) {
            return "";
        }

        StringBuilder content = new StringBuilder();

        if (vacancy.getTitle() != null && !vacancy.getTitle().trim().isEmpty()) {
            content.append(vacancy.getTitle()).append(" ");
        }
        if (vacancy.getDescription() != null && !vacancy.getDescription().trim().isEmpty()) {
            content.append(vacancy.getDescription()).append(" ");
        }
        if (vacancy.getCompany() != null && !vacancy.getCompany().trim().isEmpty()) {
            content.append(vacancy.getCompany()).append(" ");
        }
        if (vacancy.getEmploymentType() != null && !vacancy.getEmploymentType().trim().isEmpty()) {
            content.append(vacancy.getEmploymentType()).append(" ");
        }

        return content.toString();
    }

    private String buildLocation(JSearchDTO.JobData jobData) {
        if (jobData == null) {
            return "Localização não informada";
        }

        StringBuilder location = new StringBuilder();

        if (jobData.getJobCity() != null && !jobData.getJobCity().trim().isEmpty()) {
            location.append(jobData.getJobCity());
        }
        if (jobData.getJobState() != null && !jobData.getJobState().trim().isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(jobData.getJobState());
        }
        if (jobData.getJobCountry() != null && !jobData.getJobCountry().trim().isEmpty()) {
            if (location.length() > 0) location.append(", ");
            location.append(jobData.getJobCountry());
        }

        return location.length() > 0 ? location.toString() : "Localização não informada";
    }

    public List<JobVacancy> getRecentJobVacancies(int hours) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            return jobVacancyRepository.findByCreatedAtAfter(since);
        } catch (Exception e) {
            log.error("Erro ao buscar vagas recentes: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}