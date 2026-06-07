package com.unispace.dto.response;

import com.unispace.domain.room.Facility;

public record FacilityResponse(Long id, String name) {
    public static FacilityResponse from(Facility f) {
        return new FacilityResponse(f.getId(), f.getName());
    }
}
