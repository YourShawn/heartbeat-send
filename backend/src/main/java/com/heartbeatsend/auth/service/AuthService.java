package com.heartbeatsend.auth.service;

import com.heartbeatsend.auth.dto.LoginRequest;
import com.heartbeatsend.auth.dto.LoginResponse;
import com.heartbeatsend.auth.dto.UserProfileResponse;
import com.heartbeatsend.auth.entity.AppUser;
import com.heartbeatsend.auth.repository.UserRepository;
import com.heartbeatsend.common.api.ApiException;
import com.heartbeatsend.common.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim().toLowerCase();
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid username or password");
        }
        String token = jwtService.issueToken(user.getUserId(), user.getUsername());
        return new LoginResponse(token, "Bearer", jwtService.expirationMs(), toProfile(user));
    }

    public static UserProfileResponse toProfile(AppUser user) {
        return new UserProfileResponse(user.getUserId(), user.getUsername(), user.getDisplayName());
    }
}
