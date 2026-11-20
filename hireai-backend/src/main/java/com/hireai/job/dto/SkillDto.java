package com.hireai.job.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDto {
    private UUID skillId;

    @NotBlank(message = "Skill name is required")
    private String name;

    private String category;

    @Builder.Default
    private boolean required = true;

    @Builder.Default
    private int weightage = 0;
}
