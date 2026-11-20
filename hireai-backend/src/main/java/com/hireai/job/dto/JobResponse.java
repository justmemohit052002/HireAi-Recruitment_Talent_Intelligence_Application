package com.hireai.job.dto;

import com.hireai.job.enums.JobMode;
import com.hireai.job.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private UUID id;
    private UUID organizationId;
    private String title;
    private String description;
    private JobMode mode;
    private String location;
    private Integer experienceMin;
    private Integer experienceMax;
    private String interviewType;
    private Integer interviewDurationMinutes;
    private JobStatus status;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private List<SkillDto> skills = new ArrayList<>();

    @Builder.Default
    private List<JobFocusAreaDto> focusAreas = new ArrayList<>();

    @Builder.Default
    private List<SampleQuestionDto> sampleQuestions = new ArrayList<>();
}
