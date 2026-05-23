package com.tutorialregister.service;

import com.tutorialregister.dto.LoginRequest;
import com.tutorialregister.dto.LoginResponse;
import com.tutorialregister.model.UserAccount;
import com.tutorialregister.repository.UserAccountRepository;
import com.tutorialregister.security.JwtUtil;
import com.tutorialregister.web.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
        UserAccountRepository userAccountRepository,
        JwtUtil jwtUtil,
        PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse authenticate(LoginRequest request) {
        UserAccount userAccount = userAccountRepository.findByUsername(request.username())
            .orElseThrow(() -> new ResourceNotFoundException("User", request.username()));

        if (!userAccount.isEnabled()) {
            throw new IllegalArgumentException("User account is disabled");
        }

        if (!passwordEncoder.matches(request.password(), userAccount.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String roles = userAccount.getRoles().stream()
            .map(role -> role.getName())
            .reduce((r1, r2) -> r1 + "," + r2)
            .orElse("");

        String token = jwtUtil.generateToken(userAccount.getUsername(), roles);

        return new LoginResponse(
            token,
            userAccount.getUsername(),
            userAccount.getEmail(),
            roles
        );
    }
}
