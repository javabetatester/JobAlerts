package com.jobsearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class JSearchDTO {

    @Data
    public static class JobSearchResponse {
        private String status;
        @JsonProperty("request_id")
        private String requestId;
        private Parameters parameters;
        private List<JobData> data;
        private Integer count;
    }

    @Data
    public static class Parameters {
        private String query;
        private Integer page;
        @JsonProperty("num_pages")
        private Integer numPages;
    }

    @Data
    public static class JobData {
        @JsonProperty("job_id")
        private String jobId;
        @JsonProperty("employer_name")
        private String employerName;
        @JsonProperty("employer_logo")
        private String employerLogo;
        @JsonProperty("job_title")
        private String jobTitle;
        @JsonProperty("job_description")
        private String jobDescription;
        @JsonProperty("job_apply_link")
        private String jobApplyLink;
        @JsonProperty("job_city")
        private String jobCity;
        @JsonProperty("job_state")
        private String jobState;
        @JsonProperty("job_country")
        private String jobCountry;
        @JsonProperty("job_latitude")
        private Double jobLatitude;
        @JsonProperty("job_longitude")
        private Double jobLongitude;
        @JsonProperty("job_benefits")
        private List<String> jobBenefits;
        @JsonProperty("job_google_link")
        private String jobGoogleLink;
        @JsonProperty("job_offer_expiration_datetime_utc")
        private String jobOfferExpirationDatetimeUtc;
        @JsonProperty("job_offer_expiration_timestamp")
        private Long jobOfferExpirationTimestamp;
        @JsonProperty("job_posted_at_datetime_utc")
        private String jobPostedAtDatetimeUtc;
        @JsonProperty("job_posted_at_timestamp")
        private Long jobPostedAtTimestamp;
        @JsonProperty("job_posting_language")
        private String jobPostingLanguage;
        @JsonProperty("job_onet_soc")
        private String jobOnetSoc;
        @JsonProperty("job_onet_job_zone")
        private String jobOnetJobZone;
        @JsonProperty("job_occupational_categories")
        private List<String> jobOccupationalCategories;
        @JsonProperty("job_naics_code")
        private String jobNaicsCode;
        @JsonProperty("job_naics_name")
        private String jobNaicsName;
        @JsonProperty("employer_company_type")
        private String employerCompanyType;
        @JsonProperty("job_employment_type")
        private String jobEmploymentType;
        @JsonProperty("job_experience_in_place_of_education")
        private Boolean jobExperienceInPlaceOfEducation;
        @JsonProperty("job_min_salary")
        private Double jobMinSalary;
        @JsonProperty("job_max_salary")
        private Double jobMaxSalary;
        @JsonProperty("job_salary_currency")
        private String jobSalaryCurrency;
        @JsonProperty("job_salary_period")
        private String jobSalaryPeriod;
        @JsonProperty("job_highlights")
        private JobHighlights jobHighlights;
    }

    @Data
    public static class JobHighlights {
        private List<String> qualifications;
        private List<String> responsibilities;
        private List<String> benefits;
    }
}