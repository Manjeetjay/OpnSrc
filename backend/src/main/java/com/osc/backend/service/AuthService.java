package com.osc.backend.service;

import com.osc.backend.dto.AuthResponse;
import com.osc.backend.dto.LoginRequest;
import com.osc.backend.dto.RegisterRequest;
import com.osc.backend.model.User;
import com.osc.backend.repos.UserRepository;
import com.osc.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .displayName(req.getDisplayName())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }
}

