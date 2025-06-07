package com.jobsearch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

public class UserDTO {

    @Data
    public static class CreateUserRequest {
        @NotBlank(message = "Nome é obrigatório")
        private String name;

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        private String email;
    }

    @Data
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private LocalDateTime createdAt;
        private Boolean isActive;
    }
}