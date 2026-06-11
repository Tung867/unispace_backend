package com.unispace.dto.request;

import com.unispace.domain.room.Room;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

/** 공간 생성/수정 (Admin) */
public record RoomRequest(
        @NotBlank String name,
        String location,
        Integer capacity,
        String description,
        Boolean active,
        /** 건물명 */
        @Size(max = 100) String building,
        /** 공간 유형 */
        Room.RoomType roomType,
        /** 연결할 시설 ID 목록 */
        Set<Long> facilityIds
) {}
