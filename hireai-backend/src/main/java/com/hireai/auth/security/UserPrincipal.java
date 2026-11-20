package com.hireai.auth.security;

import com.hireai.auth.entity.User;
import com.hireai.auth.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final UUID organizationId;
    private final String fullName;
    private final String workEmail;
    private final String password;
    private final boolean emailVerified;
    private final AccountStatus accountStatus;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getId()))
                .collect(Collectors.toList());

        UUID orgId = user.getOrganization() != null ? user.getOrganization().getId() : null;

        return new UserPrincipal(
                user.getId(),
                orgId,
                user.getFullName(),
                user.getWorkEmail(),
                user.getPasswordHash(),
                user.isEmailVerified(),
                user.getAccountStatus(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return workEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountStatus != AccountStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return accountStatus == AccountStatus.ACTIVE;
    }
}
