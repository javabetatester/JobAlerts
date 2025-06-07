package com.jobsearch.controller;

import com.jobsearch.dto.JobAlertDTO;
import com.jobsearch.service.JobAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-alerts")
@RequiredArgsConstructor
public class JobAlertController {

    private final JobAlertService jobAlertService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<JobAlertDTO.JobAlertResponse> createJobAlert(
            @PathVariable Long userId,
            @Valid @RequestBody JobAlertDTO.CreateJobAlertRequest request) {
        JobAlertDTO.JobAlertResponse jobAlert = jobAlertService.createJobAlert(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobAlert);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<JobAlertDTO.JobAlertResponse>> getUserJobAlerts(@PathVariable Long userId) {
        List<JobAlertDTO.JobAlertResponse> alerts = jobAlertService.getUserJobAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<JobAlertDTO.JobAlertResponse> getJobAlertById(@PathVariable Long alertId) {
        JobAlertDTO.JobAlertResponse alert = jobAlertService.getJobAlertById(alertId);
        return ResponseEntity.ok(alert);
    }

    @GetMapping
    public ResponseEntity<List<JobAlertDTO.JobAlertResponse>> getAllActiveAlerts() {
        List<JobAlertDTO.JobAlertResponse> alerts = jobAlertService.getAllActiveAlerts();
        return ResponseEntity.ok(alerts);
    }

    @PutMapping("/{alertId}")
    public ResponseEntity<JobAlertDTO.JobAlertResponse> updateJobAlert(
            @PathVariable Long alertId,
            @Valid @RequestBody JobAlertDTO.CreateJobAlertRequest request) {
        JobAlertDTO.JobAlertResponse updatedAlert = jobAlertService.updateJobAlert(alertId, request);
        return ResponseEntity.ok(updatedAlert);
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deactivateJobAlert(@PathVariable Long alertId) {
        jobAlertService.deactivateJobAlert(alertId);
        return ResponseEntity.noContent().build();
    }
}