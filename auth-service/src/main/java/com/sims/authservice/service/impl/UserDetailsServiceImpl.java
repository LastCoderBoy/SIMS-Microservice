package com.sims.authservice.service.impl;

import com.sims.authservice.entity.Users;
import com.sims.authservice.repository.UserRepository;
import com.sims.authservice.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * User Details Service Implementation
 * Loads user from database for Spring Security
 *
 * @author LastCoderBoy
 * @since 2025-01-20
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Users user = userRepository.findByUsernameOrEmail(login)
                .orElseThrow(() -> new UsernameNotFoundException("No User Found: " + login));

        return new UserPrincipal(user);
    }
}