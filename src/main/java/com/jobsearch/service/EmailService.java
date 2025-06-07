package com.jobsearch.service;

import com.jobsearch.entity.JobVacancy;
import com.jobsearch.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendJobAlertEmail(User user, List<JobVacancy> matchedJobs, String alertTitle) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("🚀 Novas vagas encontradas - " + alertTitle);

            String htmlContent = buildEmailContent(user, matchedJobs, alertTitle);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email enviado para: {} com {} vagas", user.getEmail(), matchedJobs.size());

        } catch (MessagingException e) {
            log.error("Erro ao enviar email para: {}", user.getEmail(), e);
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }

    public void sendWelcomeEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Bem-vindo ao Job Alerts!");
            message.setText(String.format("""
                Olá %s!
                
                Seja bem-vindo ao Job Alerts!
                
                Agora você pode criar alertas personalizados e receber notificações 
                quando novas vagas que correspondem aos seus critérios forem encontradas.
                
                Comece criando seu primeiro alerta de emprego!
                
                Atenciosamente,
                Equipe Job Alerts
                """, user.getName()));

            mailSender.send(message);
            log.info("Email de boas-vindas enviado para: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Erro ao enviar email de boas-vindas para: {}", user.getEmail(), e);
        }
    }

    private String buildEmailContent(User user, List<JobVacancy> jobs, String alertTitle) {
        StringBuilder html = new StringBuilder();

        html.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .job-card { border: 1px solid #ddd; margin: 15px 0; padding: 15px; border-radius: 5px; }
                    .job-title { font-size: 18px; font-weight: bold; color: #2196F3; margin-bottom: 10px; }
                    .company { font-weight: bold; color: #666; }
                    .location { color: #888; }
                    .description { margin: 10px 0; }
                    .apply-btn { background: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 10px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚀 Novas Vagas Encontradas!</h1>
                        <p>Alerta: """).append(alertTitle).append("""
                        </p>
                    </div>
                    
                    <p>Olá """).append(user.getName()).append("""
                    !</p>
                    
                    <p>Encontramos """).append(jobs.size()).append("""
                     nova(s) vaga(s) que correspondem aos seus critérios:</p>
            """);

        for (JobVacancy job : jobs) {
            html.append("""
                <div class="job-card">
                    <div class="job-title">""").append(job.getTitle()).append("""
                    </div>
                    <div class="company">""").append(job.getCompany()).append("""
                    </div>
                    <div class="location">📍 """).append(job.getLocation()).append("""
                    </div>
                """);

            if (job.getDescription() != null && !job.getDescription().isEmpty()) {
                String shortDescription = job.getDescription().length() > 200
                        ? job.getDescription().substring(0, 200) + "..."
                        : job.getDescription();
                html.append("""
                    <div class="description">""").append(shortDescription).append("""
                    </div>
                    """);
            }

            if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
                html.append("""
                    <div style="color: #4CAF50; font-weight: bold;">
                        💰 $""").append(String.format("%.0f", job.getSalaryMin()))
                        .append(" - $").append(String.format("%.0f", job.getSalaryMax())).append("""
                    </div>
                    """);
            }

            if (job.getJobUrl() != null && !job.getJobUrl().isEmpty()) {
                html.append("""
                    <a href=\"""").append(job.getJobUrl()).append("""
                    " class="apply-btn" target="_blank">Ver Vaga</a>
                    """);
            }

            html.append("</div>");
        }

        html.append("""
                    <div class="footer">
                        <p>Você está recebendo este email porque tem um alerta ativo no Job Alerts.</p>
                        <p>Para gerenciar seus alertas, acesse nossa plataforma.</p>
                    </div>
                </div>
            </body>
            </html>
            """);

        return html.toString();
    }
}