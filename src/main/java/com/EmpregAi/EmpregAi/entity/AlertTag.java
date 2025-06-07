package com.jobsearch.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "alert_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tag;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_alert_id", nullable = false)
    private JobAlert jobAlert;
}