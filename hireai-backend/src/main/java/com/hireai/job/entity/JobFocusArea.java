package com.hireai.job.entity;

import com.hireai.job.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_focus_area")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobFocusArea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false, length = 200)
    private String name;

    @Builder.Default
    @Column(nullable = false)
    private int weightage = 25;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 50)
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    @Column(columnDefinition = "TEXT")
    private String topics;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
