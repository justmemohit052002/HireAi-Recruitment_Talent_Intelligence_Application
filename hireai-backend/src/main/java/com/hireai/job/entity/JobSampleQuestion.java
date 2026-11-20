package com.hireai.job.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_sample_question")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSampleQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "focus_area_id")
    private JobFocusArea focusArea;

    @Column(length = 150)
    private String category;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Builder.Default
    @Column(length = 50)
    private String difficulty = "MEDIUM";

    @Builder.Default
    @Column(name = "sort_order")
    private int sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
