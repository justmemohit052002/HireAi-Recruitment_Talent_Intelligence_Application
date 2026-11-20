package com.hireai.job.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_skill")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSkill {

    @EmbeddedId
    private JobSkillId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jobId")
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Builder.Default
    @Column(nullable = false)
    private boolean required = true;

    @Builder.Default
    @Column(nullable = false)
    private int weightage = 0;
}
