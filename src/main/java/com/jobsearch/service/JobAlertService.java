package com.jobsearch.service;

import com.jobsearch.dto.JobAlertDTO;
import com.jobsearch.entity.AlertTag;
import com.jobsearch.entity.JobAlert;
import com.jobsearch.entity.User;
import com.jobsearch.exception.JobAlertNotFoundException;
import com.jobsearch.exception.UserNotFoundException;
import com.jobsearch.repository.AlertTagRepository;
import com.jobsearch.repository.JobAlertRepository;
import com.jobsearch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobAlertService {

    private final JobAlertRepository jobAlertRepository;
    private final AlertTagRepository alertTagRepository;
    private final UserRepository userRepository;

    @Transactional
    public JobAlertDTO.JobAlertResponse createJobAlert(Long userId, JobAlertDTO.CreateJobAlertRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com ID: " + userId));

        JobAlert jobAlert = new JobAlert();
        jobAlert.setTitle(request.getTitle());
        jobAlert.setSearchQuery(request.getSearchQuery());
        jobAlert.setLocation(request.getLocation());
        jobAlert.setLocationType(request.getLocationType());
        jobAlert.setExperienceLevel(request.getExperienceLevel());
        jobAlert.setMinimumMatchingTags(request.getMinimumMatchingTags());
        jobAlert.setUser(user);

        JobAlert savedAlert = jobAlertRepository.save(jobAlert);

        Set<AlertTag> alertTags = request.getTags().stream()
                .map(tagRequest -> {
                    AlertTag alertTag = new AlertTag();
                    alertTag.setTag(tagRequest.getTag().toLowerCase().trim());
                    alertTag.setIsRequired(tagRequest.getIsRequired());
                    alertTag.setJobAlert(savedAlert);
                    return alertTag;
                })
                .collect(Collectors.toSet());

        alertTagRepository.saveAll(alertTags);
        savedAlert.setAlertTags(alertTags);

        return mapToResponse(savedAlert);
    }

    public List<JobAlertDTO.JobAlertResponse> getUserJobAlerts(Long userId) {
        List<JobAlert> alerts = jobAlertRepository.findActiveAlertsByUserId(userId);
        return alerts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public JobAlertDTO.JobAlertResponse getJobAlertById(Long alertId) {
        JobAlert alert = jobAlertRepository.findById(alertId)
                .orElseThrow(() -> new JobAlertNotFoundException("Alerta não encontrado com ID: " + alertId));
        return mapToResponse(alert);
    }

    public List<JobAlertDTO.JobAlertResponse> getAllActiveAlerts() {
        List<JobAlert> alerts = jobAlertRepository.findByIsActiveTrue();
        return alerts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public JobAlertDTO.JobAlertResponse updateJobAlert(Long alertId, JobAlertDTO.CreateJobAlertRequest request) {
        JobAlert alert = jobAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerta não encontrado"));

        alert.setTitle(request.getTitle());
        alert.setSearchQuery(request.getSearchQuery());
        alert.setLocation(request.getLocation());
        alert.setLocationType(request.getLocationType());
        alert.setExperienceLevel(request.getExperienceLevel());
        alert.setMinimumMatchingTags(request.getMinimumMatchingTags());

        alertTagRepository.deleteByJobAlertId(alertId);

        Set<AlertTag> newTags = request.getTags().stream()
                .map(tagRequest -> {
                    AlertTag alertTag = new AlertTag();
                    alertTag.setTag(tagRequest.getTag().toLowerCase().trim());
                    alertTag.setIsRequired(tagRequest.getIsRequired());
                    alertTag.setJobAlert(alert);
                    return alertTag;
                })
                .collect(Collectors.toSet());

        alertTagRepository.saveAll(newTags);
        alert.setAlertTags(newTags);

        JobAlert updatedAlert = jobAlertRepository.save(alert);
        return mapToResponse(updatedAlert);
    }



    @Transactional
    public void deactivateJobAlert(Long alertId) {
        JobAlert alert = jobAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerta não encontrado"));
        alert.setIsActive(false);
        jobAlertRepository.save(alert);
    }

    @Transactional
    public void updateLastChecked(Long alertId) {
        JobAlert alert = jobAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerta não encontrado"));
        alert.setLastChecked(LocalDateTime.now());
        jobAlertRepository.save(alert);
    }

    public JobAlert getJobAlertEntityById(Long alertId) {
        JobAlert alert = jobAlertRepository.findById(alertId)
                .orElseThrow(() -> new JobAlertNotFoundException("Alerta não encontrado com ID: " + alertId));

        // Force lazy loading of user and tags
        if (alert.getUser() != null) {
            alert.getUser().getName(); // Force load
        }
        if (alert.getAlertTags() != null) {
            alert.getAlertTags().size(); // Force load
        }

        return alert;
    }

    private JobAlertDTO.JobAlertResponse mapToResponse(JobAlert alert) {
        JobAlertDTO.JobAlertResponse response = new JobAlertDTO.JobAlertResponse();
        response.setId(alert.getId());
        response.setTitle(alert.getTitle());
        response.setSearchQuery(alert.getSearchQuery());
        response.setLocation(alert.getLocation());
        response.setLocationType(alert.getLocationType());
        response.setExperienceLevel(alert.getExperienceLevel());
        response.setMinimumMatchingTags(alert.getMinimumMatchingTags());
        response.setIsActive(alert.getIsActive());
        response.setCreatedAt(alert.getCreatedAt());
        response.setLastChecked(alert.getLastChecked());

        if (alert.getAlertTags() != null) {
            List<JobAlertDTO.TagResponse> tags = alert.getAlertTags().stream()
                    .map(tag -> {
                        JobAlertDTO.TagResponse tagResponse = new JobAlertDTO.TagResponse();
                        tagResponse.setId(tag.getId());
                        tagResponse.setTag(tag.getTag());
                        tagResponse.setIsRequired(tag.getIsRequired());
                        return tagResponse;
                    })
                    .collect(Collectors.toList());
            response.setTags(tags);
        }

        return response;
    }
}