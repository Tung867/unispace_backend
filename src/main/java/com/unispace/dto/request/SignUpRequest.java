package com.unispace.dto.request;

import jakarta.validation.constraints.*;

public record SignUpRequest(
        @NotBlank @Size(min = 4, max = 50) String username,
        @NotBlank @Size(min = 4, max = 100) String password,
        @NotBlank @Size(max = 50) String name,
        @Email String email,
        @Size(max = 100) String affiliation
) {}
