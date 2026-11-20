package com.hireai.job.repository;

import com.hireai.job.entity.Skill;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {

    Optional<Skill> findByNormalizedNameIgnoreCase(String normalizedName);

    @Query("SELECT s FROM Skill s WHERE LOWER(s.normalizedName) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY s.normalizedName ASC")
    List<Skill> searchSkills(@Param("query") String query, Pageable pageable);
}
