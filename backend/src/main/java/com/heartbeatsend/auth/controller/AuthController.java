package com.heartbeatsend.auth.controller;

import com.heartbeatsend.auth.dto.LoginRequest;
import com.heartbeatsend.auth.dto.LoginResponse;
import com.heartbeatsend.auth.dto.UserProfileResponse;
import com.heartbeatsend.auth.entity.AppUser;
import com.heartbeatsend.auth.service.AuthService;
import com.heartbeatsend.common.api.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Login with the demo account or any seeded user")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Current JWT principal")
    public UserProfileResponse me(@AuthenticationPrincipal AppUser user) {
        if (user == null) {
            throw ApiException.unauthorized("Not authenticated");
        }
        return AuthService.toProfile(user);
    }
}
