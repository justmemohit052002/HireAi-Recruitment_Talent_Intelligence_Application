package com.hireai.job.repository;

import com.hireai.job.entity.JobSkill;
import com.hireai.job.entity.JobSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, JobSkillId> {
    void deleteByJobId(UUID jobId);
}
