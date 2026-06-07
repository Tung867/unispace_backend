package com.unispace.dto.response;

import java.util.List;

/** 공간의 시설 + 해당 기간 점유 시간대 현황 */
public record RoomAvailabilityResponse(
        Long roomId,
        String roomName,
        boolean active,
        List<FacilityResponse> facilities,
        List<TimeSlotResponse> occupiedSlots
) {}
