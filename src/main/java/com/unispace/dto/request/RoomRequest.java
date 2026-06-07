package com.unispace.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

/** 공간 생성/수정 (Admin) */
public record RoomRequest(
        @NotBlank String name,
        String location,
        Integer capacity,
        String description,
        Boolean active,
        /** 연결할 시설 ID 목록 */
        Set<Long> facilityIds
) {}
