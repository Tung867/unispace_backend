package com.unispace.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FacilityRequest(
        @NotBlank String name
) {}
