package com.jobsearch.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_job_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "job_vacancy_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserJobHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_vacancy_id", nullable = false)
    private JobVacancy jobVacancy;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "alert_title")
    private String alertTitle;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}