package com.heartbeatsend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(min = 2, max = 64) String username,
        @NotBlank @Size(min = 6, max = 72) String password
) {
}
