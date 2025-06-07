package com.jobsearch.controller;

import com.jobsearch.entity.JobVacancy;
import com.jobsearch.entity.User;
import com.jobsearch.service.EmailService;
import com.jobsearch.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;
    private final UserService userService;

    @PostMapping("/test/{userId}")
    public ResponseEntity<String> sendTestEmail(@PathVariable Long userId) {
        try {
            var userResponse = userService.getUserById(userId);

            User user = new User();
            user.setId(userResponse.getId());
            user.setName(userResponse.getName());
            user.setEmail(userResponse.getEmail());

            List<JobVacancy> testJobs = createTestJobs();

            emailService.sendJobAlertEmail(user, testJobs, "Teste de Alerta de Emprego");

            return ResponseEntity.ok("Email de teste enviado para: " + user.getEmail());

        } catch (Exception e) {
            log.error("Erro ao enviar email de teste: ", e);
            return ResponseEntity.badRequest()
                    .body("Erro ao enviar email: " + e.getMessage());
        }
    }

    @PostMapping("/welcome/{userId}")
    public ResponseEntity<String> sendWelcomeEmail(@PathVariable Long userId) {
        try {
            var userResponse = userService.getUserById(userId);

            User user = new User();
            user.setId(userResponse.getId());
            user.setName(userResponse.getName());
            user.setEmail(userResponse.getEmail());

            emailService.sendWelcomeEmail(user);

            return ResponseEntity.ok("Email de boas-vindas enviado para: " + user.getEmail());

        } catch (Exception e) {
            log.error("Erro ao enviar email de boas-vindas: ", e);
            return ResponseEntity.badRequest()
                    .body("Erro ao enviar email: " + e.getMessage());
        }
    }

    @PostMapping("/test-manual")
    public ResponseEntity<String> sendTestEmailManual(
            @RequestParam String email,
            @RequestParam String name) {
        try {
            User user = new User();
            user.setName(name);
            user.setEmail(email);

            List<JobVacancy> testJobs = createTestJobs();

            emailService.sendJobAlertEmail(user, testJobs, "Teste Manual de Alerta");

            return ResponseEntity.ok("Email de teste enviado para: " + email);

        } catch (Exception e) {
            log.error("Erro ao enviar email de teste manual: ", e);
            return ResponseEntity.badRequest()
                    .body("Erro ao enviar email: " + e.getMessage());
        }
    }

    private List<JobVacancy> createTestJobs() {
        JobVacancy job1 = new JobVacancy();
        job1.setExternalId("test-001");
        job1.setTitle("Desenvolvedor Java Sênior");
        job1.setCompany("Tech Company Inc.");
        job1.setLocation("São Paulo, SP, Brasil");
        job1.setDescription("Vaga para desenvolvedor Java com experiência em Spring Boot, microservices e AWS.");
        job1.setJobUrl("https://example.com/job1");
        job1.setSalaryMin(8000.0);
        job1.setSalaryMax(12000.0);
        job1.setEmploymentType("Full-time");
        job1.setPublishedAt(LocalDateTime.now().minusHours(2));

        JobVacancy job2 = new JobVacancy();
        job2.setExternalId("test-002");
        job2.setTitle("Desenvolvedor Full Stack");
        job2.setCompany("Startup Inovadora");
        job2.setLocation("Remote, Brasil");
        job2.setDescription("Oportunidade para trabalhar com React, Node.js e MongoDB em um ambiente ágil.");
        job2.setJobUrl("https://example.com/job2");
        job2.setSalaryMin(6000.0);
        job2.setSalaryMax(10000.0);
        job2.setEmploymentType("Full-time");
        job2.setPublishedAt(LocalDateTime.now().minusHours(4));

        return Arrays.asList(job1, job2);
    }
}