package com.unispace.dto.request;

import com.unispace.domain.user.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(
        @NotNull Role role
) {}
