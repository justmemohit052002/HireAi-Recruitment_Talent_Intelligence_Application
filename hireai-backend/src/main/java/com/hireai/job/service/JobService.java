package com.hireai.job.service;

import com.hireai.job.dto.*;
import com.hireai.job.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JobService {

    JobResponse createJob(UUID organizationId, UUID userId, CreateJobRequest request);

    JobResponse getJobById(UUID organizationId, UUID jobId);

    Page<JobSummaryResponse> listJobs(UUID organizationId, JobStatus status, String search, Pageable pageable);

    JobResponse updateJob(UUID organizationId, UUID jobId, UpdateJobRequest request);

    JobResponse updateJobStatus(UUID organizationId, UUID jobId, JobStatus status);

    void deleteJob(UUID organizationId, UUID jobId);

    JobDashboardStats getDashboardStats(UUID organizationId);
}
