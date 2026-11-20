package com.hireai.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDashboardStats {
    private long openJobRoles;
    private long onHoldJobRoles;
    private long closedJobRoles;
    private long draftJobRoles;
    private long totalJobRoles;
}
