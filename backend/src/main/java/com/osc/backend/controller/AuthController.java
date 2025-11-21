package com.osc.backend.controller;

import com.osc.backend.dto.AuthResponse;
import com.osc.backend.dto.LoginRequest;
import com.osc.backend.dto.RegisterRequest;
import com.osc.backend.model.User;
import com.osc.backend.repos.UserRepository;
import com.osc.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/set-github-username")
    public ResponseEntity<?> setGithubUsername(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {

        if (user == null) return ResponseEntity.status(401).body("Unauthorized");

        String username = body.get("githubUsername");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("githubUsername required");
        }

        user.setGithubUsername(username);
        userRepository.save(user);

        return ResponseEntity.ok("GitHub username saved");
    }

}
