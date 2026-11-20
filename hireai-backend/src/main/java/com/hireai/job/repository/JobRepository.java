package com.hireai.job.repository;

import com.hireai.job.entity.Job;
import com.hireai.job.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    Optional<Job> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("SELECT j FROM Job j LEFT JOIN FETCH j.skills js LEFT JOIN FETCH js.skill " +
           "LEFT JOIN FETCH j.focusAreas LEFT JOIN FETCH j.sampleQuestions " +
           "WHERE j.id = :id AND j.organization.id = :orgId")
    Optional<Job> findByIdAndOrganizationIdWithDetails(@Param("id") UUID id, @Param("orgId") UUID orgId);

    Page<Job> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<Job> findByOrganizationIdAndStatus(UUID organizationId, JobStatus status, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.organization.id = :orgId " +
           "AND (:status IS NULL OR j.status = :status) " +
           "AND (:search IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(j.location) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Job> searchJobs(
            @Param("orgId") UUID organizationId,
            @Param("status") JobStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    long countByOrganizationIdAndStatus(UUID organizationId, JobStatus status);

    long countByOrganizationId(UUID organizationId);
}
