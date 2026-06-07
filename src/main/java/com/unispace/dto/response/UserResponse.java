package com.unispace.dto.response;

import com.unispace.domain.user.Role;
import com.unispace.domain.user.User;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String name,
        String email,
        String affiliation,
        Role role,
        LocalDateTime createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getName(),
                u.getEmail(), u.getAffiliation(), u.getRole(), u.getCreatedAt());
    }
}
