package com.unispace.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(max = 50) String name,
        @Email String email,
        @Size(max = 100) String affiliation
) {}
