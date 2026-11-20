package com.hireai.job.dto;

import com.hireai.job.enums.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusUpdateRequest {

    @NotNull(message = "Job status is required")
    private JobStatus status;
}
