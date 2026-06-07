package com.unispace.dto.response;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInMs
) {
    public static TokenResponse bearer(String token, long expiresInMs) {
        return new TokenResponse(token, "Bearer", expiresInMs);
    }
}
