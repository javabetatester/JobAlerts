package com.jobsearch.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "job_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"alertTags", "user"})
@ToString(exclude = {"alertTags", "user"})
public class JobAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "search_query", nullable = false)
    private String searchQuery;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false)
    private LocationType locationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel;

    @Column(name = "minimum_matching_tags", nullable = false)
    private Integer minimumMatchingTags;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_checked")
    private LocalDateTime lastChecked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "jobAlert", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AlertTag> alertTags;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum LocationType {
        REMOTO,
        HIBRIDO,
        PRESENCIAL,
        QUALQUER,
        REMOTE,
        HYBRID,
        ONSITE,
        ANY
    }

    public enum ExperienceLevel {
        ESTAGIARIO,
        JR,
        JUNIOR,
        PL,
        PLENO,
        SR,
        SENIOR,
        ESPECIALISTA,
        LEAD,
        COORDENADOR,
        GERENTE,
        DIRETOR,
        ENTRY_LEVEL,
        MID_LEVEL,
        SENIOR_LEVEL,
        EXECUTIVE,
        ANY,
        QUALQUER
    }
}