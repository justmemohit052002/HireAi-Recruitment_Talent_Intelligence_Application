package com.hireai.job.dto;

import com.hireai.job.enums.DifficultyLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class JobFocusAreaDto {
    private UUID id;

    @NotBlank(message = "Focus area name is required")
    private String name;

    @Min(value = 0, message = "Weightage cannot be less than 0%")
    @Max(value = 100, message = "Weightage cannot exceed 100%")
    @Builder.Default
    private int weightage = 25;

    @Builder.Default
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    private String topics;
}
