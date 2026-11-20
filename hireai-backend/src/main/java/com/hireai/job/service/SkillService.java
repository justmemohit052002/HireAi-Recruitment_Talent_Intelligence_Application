package com.hireai.job.service;

import com.hireai.job.dto.SkillDto;
import com.hireai.job.entity.Skill;

import java.util.List;

public interface SkillService {

    Skill getOrCreateSkill(String name, String category);

    List<SkillDto> searchSkills(String query, int limit);
}
