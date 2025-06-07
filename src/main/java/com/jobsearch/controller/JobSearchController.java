package com.jobsearch.controller;

import com.jobsearch.dto.JSearchDTO;
import com.jobsearch.entity.JobVacancy;
import com.jobsearch.service.JSearchService;
import com.jobsearch.service.JobMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-search")
@RequiredArgsConstructor
public class JobSearchController {

    private final JSearchService jSearchService;
    private final JobMatchingService jobMatchingService;

    @GetMapping("/search")
    public ResponseEntity<JSearchDTO.JobSearchResponse> searchJobs(
            @RequestParam String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "1") Integer page) {

        JSearchDTO.JobSearchResponse response = jSearchService.searchJobs(query, location, page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<JSearchDTO.JobSearchResponse> searchJobsWithFilters(
            @RequestParam String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false, defaultValue = "1") Integer page) {

        JSearchDTO.JobSearchResponse response = jSearchService.searchJobsWithFilters(query, location, employmentType, page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent-vacancies")
    public ResponseEntity<List<JobVacancy>> getRecentJobVacancies(
            @RequestParam(required = false, defaultValue = "24") Integer hours) {

        List<JobVacancy> recentJobs = jobMatchingService.getRecentJobVacancies(hours);
        return ResponseEntity.ok(recentJobs);
    }
}