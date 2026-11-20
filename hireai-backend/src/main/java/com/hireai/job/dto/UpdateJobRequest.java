package com.hireai.job.dto;

import com.hireai.job.enums.JobMode;
import com.hireai.job.enums.JobStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    private JobMode mode;

    private String location;

    @Min(value = 0, message = "Minimum experience must be non-negative")
    private Integer experienceMin;

    @Min(value = 0, message = "Maximum experience must be non-negative")
    private Integer experienceMax;

    private String interviewType;

    private Integer interviewDurationMinutes;

    private JobStatus status;

    @Valid
    @Builder.Default
    private List<SkillDto> skills = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<JobFocusAreaDto> focusAreas = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<SampleQuestionDto> sampleQuestions = new ArrayList<>();
}
