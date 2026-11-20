package com.hireai.job.repository;

import com.hireai.job.entity.JobSampleQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobSampleQuestionRepository extends JpaRepository<JobSampleQuestion, UUID> {
    List<JobSampleQuestion> findByJobIdOrderBySortOrderAsc(UUID jobId);
    void deleteByJobId(UUID jobId);
}
