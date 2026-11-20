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
public class SampleQuestionDto {
    private UUID id;
    private UUID focusAreaId;
    private String category;

    @NotBlank(message = "Question text is required")
    private String questionText;

    @Builder.Default
    private String difficulty = "MEDIUM";

    @Builder.Default
    private int sortOrder = 0;
}
