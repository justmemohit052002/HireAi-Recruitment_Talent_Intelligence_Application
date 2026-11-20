package com.hireai.job.repository;

import com.hireai.job.entity.JobFocusArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobFocusAreaRepository extends JpaRepository<JobFocusArea, UUID> {
    List<JobFocusArea> findByJobId(UUID jobId);
    void deleteByJobId(UUID jobId);
}
