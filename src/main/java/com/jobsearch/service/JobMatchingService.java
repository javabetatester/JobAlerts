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

        if (searchResponse.getData() == null || searchResponse.getData().isEmpty()) {
            log.info("Nenhuma vaga encontrada na busca para o alerta: {}", jobAlert.getTitle());
            return matchedJobs;
        }

        for (JSearchDTO.JobData jobData : searchResponse.getData()) {
            try {
                JobVacancy vacancy = saveOrUpdateJobVacancy(jobData);

                if (isJobMatching(vacancy, jobAlert)) {
                    matchedJobs.add(vacancy);
                    log.info("Vaga matched: {} - {}", vacancy.getTitle(), vacancy.getCompany());
                }

            } catch (Exception e) {
                log.error("Erro ao processar vaga: {}", jobData.getJobTitle(), e);
            }
        }

        log.info("Processadas {} vagas, {} matches para alerta: {}",
                searchResponse.getData().size(), matchedJobs.size(), jobAlert.getTitle());

        return matchedJobs;
    }

    private JobVacancy saveOrUpdateJobVacancy(JSearchDTO.JobData jobData) {
        JobVacancy vacancy = jobVacancyRepository.findByExternalId(jobData.getJobId())
                .orElse(new JobVacancy());

        vacancy.setExternalId(jobData.getJobId());
        vacancy.setTitle(jobData.getJobTitle());
        vacancy.setCompany(jobData.getEmployerName());
        vacancy.setLocation(buildLocation(jobData));
        vacancy.setDescription(jobData.getJobDescription());
        vacancy.setJobUrl(jobData.getJobApplyLink());
        vacancy.setSalaryMin(jobData.getJobMinSalary());
        vacancy.setSalaryMax(jobData.getJobMaxSalary());
        vacancy.setEmploymentType(jobData.getJobEmploymentType());

        if (jobData.getJobPostedAtDatetimeUtc() != null) {
            try {
                vacancy.setPublishedAt(LocalDateTime.parse(
                        jobData.getJobPostedAtDatetimeUtc().replace("Z", ""),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                ));
            } catch (Exception e) {
                log.warn("Erro ao parsear data de publicação: {}", jobData.getJobPostedAtDatetimeUtc());
            }
        }

        return jobVacancyRepository.save(vacancy);
    }

    private boolean isJobMatching(JobVacancy vacancy, JobAlert jobAlert) {
        Set<AlertTag> alertTags = jobAlert.getAlertTags();
        if (alertTags == null || alertTags.isEmpty()) {
            return false;
        }

        String jobContent = buildJobContent(vacancy).toLowerCase();

        List<AlertTag> requiredTags = alertTags.stream()
                .filter(AlertTag::getIsRequired)
                .collect(Collectors.toList());

        for (AlertTag requiredTag : requiredTags) {
            if (!jobContent.contains(requiredTag.getTag().toLowerCase())) {
                log.debug("Vaga {} não possui tag obrigatória: {}", vacancy.getTitle(), requiredTag.getTag());
                return false;
            }
        }

        long matchingTagsCount = alertTags.stream()
                .filter(tag -> jobContent.contains(tag.getTag().toLowerCase()))
                .count();

        boolean hasMinimumTags = matchingTagsCount >= jobAlert.getMinimumMatchingTags();

        if (hasMinimumTags) {
            log.debug("Vaga {} matched com {} tags (mínimo: {})",
                    vacancy.getTitle(), matchingTagsCount, jobAlert.getMinimumMatchingTags());
        } else {
            log.debug("Vaga {} não atingiu mínimo de tags: {} de {} necessárias",
                    vacancy.getTitle(), matchingTagsCount, jobAlert.getMinimumMatchingTags());
        }

        return hasMinimumTags;
    }

    private String buildJobContent(JobVacancy vacancy) {
        StringBuilder content = new StringBuilder();

        if (vacancy.getTitle() != null) {
            content.append(vacancy.getTitle()).append(" ");
        }
        if (vacancy.getDescription() != null) {
            content.append(vacancy.getDescription()).append(" ");
        }
        if (vacancy.getCompany() != null) {
            content.append(vacancy.getCompany()).append(" ");
        }
        if (vacancy.getEmploymentType() != null) {
            content.append(vacancy.getEmploymentType()).append(" ");
        }

        return content.toString();
    }

    private String buildLocation(JSearchDTO.JobData jobData) {
        StringBuilder location = new StringBuilder();

        if (jobData.getJobCity() != null) {
            location.append(jobData.getJobCity());
        }
        if (jobData.getJobState() != null) {
            if (location.length() > 0) location.append(", ");
            location.append(jobData.getJobState());
        }
        if (jobData.getJobCountry() != null) {
            if (location.length() > 0) location.append(", ");
            location.append(jobData.getJobCountry());
        }

        return location.toString();
    }

    public List<JobVacancy> getRecentJobVacancies(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return jobVacancyRepository.findByCreatedAtAfter(since);
    }
}