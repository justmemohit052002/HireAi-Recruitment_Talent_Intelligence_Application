package com.hireai.auth.dto;

import com.hireai.auth.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private String fullName;
    private String workEmail;
    private boolean emailVerified;
    private AccountStatus accountStatus;
    private Set<String> roles;
}
