package com.osc.backend.controller;

import com.osc.backend.dto.AuthResponse;
import com.osc.backend.dto.LoginRequest;
import com.osc.backend.dto.RegisterRequest;
import com.osc.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
