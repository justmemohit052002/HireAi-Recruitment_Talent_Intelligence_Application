package com.hireai.auth.security;

import com.hireai.auth.entity.User;
import com.hireai.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String workEmail) throws UsernameNotFoundException {
        User user = userRepository.findByWorkEmailWithDetails(workEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with work email: " + workEmail));

        return UserPrincipal.create(user);
    }
}
