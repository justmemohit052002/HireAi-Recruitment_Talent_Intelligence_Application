package com.hireai.job.service.impl;

import com.hireai.auth.entity.Organization;
import com.hireai.auth.entity.User;
import com.hireai.auth.repository.OrganizationRepository;
import com.hireai.auth.repository.UserRepository;
import com.hireai.common.exception.ResourceNotFoundException;
import com.hireai.job.dto.*;
import com.hireai.job.entity.*;
import com.hireai.job.enums.DifficultyLevel;
import com.hireai.job.enums.JobMode;
import com.hireai.job.enums.JobStatus;
import com.hireai.job.repository.JobRepository;
import com.hireai.job.service.JobService;
import com.hireai.job.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final SkillService skillService;

    @Override
    @Transactional
    public JobResponse createJob(UUID organizationId, UUID userId, CreateJobRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));

        User createdBy = null;
        if (userId != null) {
            createdBy = userRepository.findById(userId).orElse(null);
        }

        Job job = Job.builder()
                .organization(organization)
                .createdBy(createdBy)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .mode(request.getMode() != null ? request.getMode() : JobMode.SMART)
                .location(request.getLocation() != null ? request.getLocation().trim() : null)
                .experienceMin(request.getExperienceMin())
                .experienceMax(request.getExperienceMax())
                .interviewType(request.getInterviewType() != null ? request.getInterviewType() : "Core Knowledge Evaluation")
                .interviewDurationMinutes(request.getInterviewDurationMinutes() != null ? request.getInterviewDurationMinutes() : 30)
                .status(JobStatus.OPEN)
                .skills(new ArrayList<>())
                .focusAreas(new ArrayList<>())
                .sampleQuestions(new ArrayList<>())
                .build();

        // 1. Process and attach skills
        if (request.getSkills() != null) {
            for (SkillDto sDto : request.getSkills()) {
                Skill skill = skillService.getOrCreateSkill(sDto.getName(), sDto.getCategory());
                JobSkill jobSkill = JobSkill.builder()
                        .id(new JobSkillId(job.getId(), skill.getId()))
                        .job(job)
                        .skill(skill)
                        .required(sDto.isRequired())
                        .weightage(sDto.getWeightage())
                        .build();
                job.getSkills().add(jobSkill);
            }
        }

        // 2. Process and attach custom focus areas (for Custom mode / rubric scoring)
        Map<String, JobFocusArea> focusAreaMap = new HashMap<>();
        if (request.getFocusAreas() != null) {
            for (JobFocusAreaDto faDto : request.getFocusAreas()) {
                JobFocusArea focusArea = JobFocusArea.builder()
                        .job(job)
                        .name(faDto.getName().trim())
                        .weightage(faDto.getWeightage())
                        .difficultyLevel(faDto.getDifficultyLevel() != null ? faDto.getDifficultyLevel() : DifficultyLevel.MEDIUM)
                        .topics(faDto.getTopics())
                        .build();
                job.getFocusAreas().add(focusArea);
                focusAreaMap.put(focusArea.getName().toLowerCase(), focusArea);
            }
        }

        // 3. Process and attach suggested / configured interview questions
        if (request.getSampleQuestions() != null) {
            for (int i = 0; i < request.getSampleQuestions().size(); i++) {
                SampleQuestionDto qDto = request.getSampleQuestions().get(i);
                JobFocusArea linkedArea = null;
                if (qDto.getCategory() != null) {
                    linkedArea = focusAreaMap.get(qDto.getCategory().trim().toLowerCase());
                }

                JobSampleQuestion question = JobSampleQuestion.builder()
                        .job(job)
                        .focusArea(linkedArea)
                        .category(qDto.getCategory())
                        .questionText(qDto.getQuestionText().trim())
                        .difficulty(qDto.getDifficulty() != null ? qDto.getDifficulty() : "MEDIUM")
                        .sortOrder(qDto.getSortOrder() != 0 ? qDto.getSortOrder() : i + 1)
                        .build();
                job.getSampleQuestions().add(question);
            }
        }

        job = jobRepository.save(job);
        log.info("Created job '{}' (mode: {}) for organization {}", job.getTitle(), job.getMode(), organization.getName());

        return mapToResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(UUID organizationId, UUID jobId) {
        Job job = jobRepository.findByIdAndOrganizationIdWithDetails(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
        return mapToResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobSummaryResponse> listJobs(UUID organizationId, JobStatus status, String search, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<Job> jobPage = jobRepository.searchJobs(organizationId, status, cleanSearch, pageable);

        return jobPage.map(job -> JobSummaryResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .location(job.getLocation())
                .mode(job.getMode())
                .status(job.getStatus())
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .skillsCount(job.getSkills() != null ? job.getSkills().size() : 0)
                .focusAreasCount(job.getFocusAreas() != null ? job.getFocusAreas().size() : 0)
                .createdAt(job.getCreatedAt())
                .build());
    }

    @Override
    @Transactional
    public JobResponse updateJob(UUID organizationId, UUID jobId, UpdateJobRequest request) {
        Job job = jobRepository.findByIdAndOrganizationIdWithDetails(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        if (request.getMode() != null) job.setMode(request.getMode());
        if (request.getLocation() != null) job.setLocation(request.getLocation().trim());
        job.setExperienceMin(request.getExperienceMin());
        job.setExperienceMax(request.getExperienceMax());
        if (request.getInterviewType() != null) job.setInterviewType(request.getInterviewType());
        if (request.getInterviewDurationMinutes() != null) job.setInterviewDurationMinutes(request.getInterviewDurationMinutes());
        if (request.getStatus() != null) job.setStatus(request.getStatus());

        // Update skills if supplied
        if (request.getSkills() != null) {
            job.getSkills().clear();
            for (SkillDto sDto : request.getSkills()) {
                Skill skill = skillService.getOrCreateSkill(sDto.getName(), sDto.getCategory());
                JobSkill jobSkill = JobSkill.builder()
                        .id(new JobSkillId(job.getId(), skill.getId()))
                        .job(job)
                        .skill(skill)
                        .required(sDto.isRequired())
                        .weightage(sDto.getWeightage())
                        .build();
                job.getSkills().add(jobSkill);
            }
        }

        // Update focus areas if supplied
        Map<String, JobFocusArea> focusAreaMap = new HashMap<>();
        if (request.getFocusAreas() != null) {
            job.getFocusAreas().clear();
            for (JobFocusAreaDto faDto : request.getFocusAreas()) {
                JobFocusArea focusArea = JobFocusArea.builder()
                        .job(job)
                        .name(faDto.getName().trim())
                        .weightage(faDto.getWeightage())
                        .difficultyLevel(faDto.getDifficultyLevel() != null ? faDto.getDifficultyLevel() : DifficultyLevel.MEDIUM)
                        .topics(faDto.getTopics())
                        .build();
                job.getFocusAreas().add(focusArea);
                focusAreaMap.put(focusArea.getName().toLowerCase(), focusArea);
            }
        }

        // Update sample questions if supplied
        if (request.getSampleQuestions() != null) {
            job.getSampleQuestions().clear();
            for (int i = 0; i < request.getSampleQuestions().size(); i++) {
                SampleQuestionDto qDto = request.getSampleQuestions().get(i);
                JobFocusArea linkedArea = null;
                if (qDto.getCategory() != null) {
                    linkedArea = focusAreaMap.get(qDto.getCategory().trim().toLowerCase());
                }

                JobSampleQuestion question = JobSampleQuestion.builder()
                        .job(job)
                        .focusArea(linkedArea)
                        .category(qDto.getCategory())
                        .questionText(qDto.getQuestionText().trim())
                        .difficulty(qDto.getDifficulty() != null ? qDto.getDifficulty() : "MEDIUM")
                        .sortOrder(qDto.getSortOrder() != 0 ? qDto.getSortOrder() : i + 1)
                        .build();
                job.getSampleQuestions().add(question);
            }
        }

        job = jobRepository.save(job);
        return mapToResponse(job);
    }

    @Override
    @Transactional
    public JobResponse updateJobStatus(UUID organizationId, UUID jobId, JobStatus status) {
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        job.setStatus(status);
        job = jobRepository.save(job);
        return mapToResponse(job);
    }

    @Override
    @Transactional
    public void deleteJob(UUID organizationId, UUID jobId) {
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
        jobRepository.delete(job);
    }

    @Override
    @Transactional(readOnly = true)
    public JobDashboardStats getDashboardStats(UUID organizationId) {
        long openCount = jobRepository.countByOrganizationIdAndStatus(organizationId, JobStatus.OPEN);
        long onHoldCount = jobRepository.countByOrganizationIdAndStatus(organizationId, JobStatus.ON_HOLD);
        long closedCount = jobRepository.countByOrganizationIdAndStatus(organizationId, JobStatus.CLOSED);
        long draftCount = jobRepository.countByOrganizationIdAndStatus(organizationId, JobStatus.DRAFT);
        long totalCount = jobRepository.countByOrganizationId(organizationId);

        return JobDashboardStats.builder()
                .openJobRoles(openCount)
                .onHoldJobRoles(onHoldCount)
                .closedJobRoles(closedCount)
                .draftJobRoles(draftCount)
                .totalJobRoles(totalCount)
                .build();
    }

    private JobResponse mapToResponse(Job job) {
        List<SkillDto> skills = job.getSkills() != null ? job.getSkills().stream()
                .map(js -> SkillDto.builder()
                        .skillId(js.getSkill().getId())
                        .name(js.getSkill().getNormalizedName())
                        .category(js.getSkill().getCategory())
                        .required(js.isRequired())
                        .weightage(js.getWeightage())
                        .build())
                .collect(Collectors.toList()) : Collections.emptyList();

        List<JobFocusAreaDto> focusAreas = job.getFocusAreas() != null ? job.getFocusAreas().stream()
                .map(fa -> JobFocusAreaDto.builder()
                        .id(fa.getId())
                        .name(fa.getName())
                        .weightage(fa.getWeightage())
                        .difficultyLevel(fa.getDifficultyLevel())
                        .topics(fa.getTopics())
                        .build())
                .collect(Collectors.toList()) : Collections.emptyList();

        List<SampleQuestionDto> sampleQuestions = job.getSampleQuestions() != null ? job.getSampleQuestions().stream()
                .map(sq -> SampleQuestionDto.builder()
                        .id(sq.getId())
                        .focusAreaId(sq.getFocusArea() != null ? sq.getFocusArea().getId() : null)
                        .category(sq.getCategory())
                        .questionText(sq.getQuestionText())
                        .difficulty(sq.getDifficulty())
                        .sortOrder(sq.getSortOrder())
                        .build())
                .collect(Collectors.toList()) : Collections.emptyList();

        String createdByName = job.getCreatedBy() != null ? job.getCreatedBy().getFullName() : null;

        return JobResponse.builder()
                .id(job.getId())
                .organizationId(job.getOrganization().getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .mode(job.getMode())
                .location(job.getLocation())
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .interviewType(job.getInterviewType())
                .interviewDurationMinutes(job.getInterviewDurationMinutes())
                .status(job.getStatus())
                .createdByName(createdByName)
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .skills(skills)
                .focusAreas(focusAreas)
                .sampleQuestions(sampleQuestions)
                .build();
    }
}
