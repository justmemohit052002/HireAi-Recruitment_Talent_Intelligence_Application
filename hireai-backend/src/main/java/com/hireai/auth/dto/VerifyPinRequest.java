package com.hireai.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPinRequest {

    @NotBlank(message = "Work email is required")
    @Email(message = "Please provide a valid email address")
    private String workEmail;

    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN must be a 6-digit number")
    private String pin;
}
