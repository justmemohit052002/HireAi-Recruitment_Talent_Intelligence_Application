package com.hireai.job.controller;

import com.hireai.auth.security.UserPrincipal;
import com.hireai.common.dto.ApiResponse;
import com.hireai.job.dto.*;
import com.hireai.job.enums.JobStatus;
import com.hireai.job.service.JobService;
import com.hireai.job.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final SkillService skillService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateJobRequest request
    ) {
        JobResponse response = jobService.createJob(currentUser.getOrganizationId(), currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job role created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobSummaryResponse>>> listJobs(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Page<JobSummaryResponse> jobs = jobService.listJobs(
                currentUser.getOrganizationId(),
                status,
                search,
                PageRequest.of(page, size, sort)
        );
        return ResponseEntity.ok(ApiResponse.success("Job roles fetched successfully.", jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        JobResponse response = jobService.getJobById(currentUser.getOrganizationId(), id);
        return ResponseEntity.ok(ApiResponse.success("Job details retrieved.", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJobRequest request
    ) {
        JobResponse response = jobService.updateJob(currentUser.getOrganizationId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Job role updated successfully.", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJobStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody JobStatusUpdateRequest request
    ) {
        JobResponse response = jobService.updateJobStatus(currentUser.getOrganizationId(), id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Job status updated to " + request.getStatus() + ".", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORG_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id
    ) {
        jobService.deleteJob(currentUser.getOrganizationId(), id);
        return ResponseEntity.ok(ApiResponse.success("Job role deleted successfully."));
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<JobDashboardStats>> getDashboardStats(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        JobDashboardStats stats = jobService.getDashboardStats(currentUser.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success("Job dashboard stats retrieved.", stats));
    }

    @GetMapping("/skills/search")
    public ResponseEntity<ApiResponse<List<SkillDto>>> searchSkills(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<SkillDto> skills = skillService.searchSkills(query, limit);
        return ResponseEntity.ok(ApiResponse.success("Matching skills retrieved.", skills));
    }
}
