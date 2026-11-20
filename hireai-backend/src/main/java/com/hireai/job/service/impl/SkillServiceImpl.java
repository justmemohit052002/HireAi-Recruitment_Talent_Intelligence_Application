package com.hireai.job.service.impl;

import com.hireai.job.dto.SkillDto;
import com.hireai.job.entity.Skill;
import com.hireai.job.repository.SkillRepository;
import com.hireai.job.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    @Override
    @Transactional
    public Skill getOrCreateSkill(String name, String category) {
        String cleanName = name.trim();
        return skillRepository.findByNormalizedNameIgnoreCase(cleanName)
                .orElseGet(() -> skillRepository.save(
                        Skill.builder()
                                .normalizedName(cleanName)
                                .category(category != null ? category.trim() : "General")
                                .build()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillDto> searchSkills(String query, int limit) {
        return skillRepository.searchSkills(query.trim(), PageRequest.of(0, limit)).stream()
                .map(s -> SkillDto.builder()
                        .skillId(s.getId())
                        .name(s.getNormalizedName())
                        .category(s.getCategory())
                        .build())
                .collect(Collectors.toList());
    }
}
