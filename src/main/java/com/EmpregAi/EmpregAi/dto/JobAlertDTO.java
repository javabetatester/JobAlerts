package com.jobsearch.dto;

import com.jobsearch.entity.JobAlert;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class JobAlertDTO {

    @Data
    public static class CreateJobAlertRequest {
        @NotBlank(message = "Título é obrigatório")
        private String title;

        @NotBlank(message = "Query de busca é obrigatória")
        private String searchQuery;

        @NotBlank(message = "Localização é obrigatória")
        private String location;

        @NotNull(message = "Tipo de localização é obrigatório")
        private JobAlert.LocationType locationType;

        private JobAlert.ExperienceLevel experienceLevel;

        @Min(value = 1, message = "Mínimo de tags deve ser pelo menos 1")
        private Integer minimumMatchingTags;

        @NotEmpty(message = "Pelo menos uma tag é obrigatória")
        private List<TagRequest> tags;
    }

    @Data
    public static class TagRequest {
        @NotBlank(message = "Tag é obrigatória")
        private String tag;

        private Boolean isRequired = false;
    }

    @Data
    public static class JobAlertResponse {
        private Long id;
        private String title;
        private String searchQuery;
        private String location;
        private JobAlert.LocationType locationType;
        private JobAlert.ExperienceLevel experienceLevel;
        private Integer minimumMatchingTags;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime lastChecked;
        private List<TagResponse> tags;
    }

    @Data
    public static class TagResponse {
        private Long id;
        private String tag;
        private Boolean isRequired;
    }
}