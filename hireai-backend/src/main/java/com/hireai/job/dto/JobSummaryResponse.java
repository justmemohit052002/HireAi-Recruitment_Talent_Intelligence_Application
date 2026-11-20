package com.hireai.job.dto;

import com.hireai.job.enums.JobMode;
import com.hireai.job.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSummaryResponse {

    private UUID id;
    private String title;
    private String location;
    private JobMode mode;
    private JobStatus status;
    private Integer experienceMin;
    private Integer experienceMax;
    private int skillsCount;
    private int focusAreasCount;
    private Instant createdAt;
}
