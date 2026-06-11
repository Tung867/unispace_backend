package com.unispace.dto.response;

import com.unispace.domain.room.Room;
import java.util.List;

public record RoomResponse(
        Long id,
        String name,
        String location,
        Integer capacity,
        String description,
        boolean active,
        String building,
        String roomType,
        List<FacilityResponse> facilities
) {
    public static RoomResponse from(Room r) {
        List<FacilityResponse> fac = r.getFacilities().stream()
                .map(FacilityResponse::from)
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .toList();
        return new RoomResponse(r.getId(), r.getName(), r.getLocation(),
                r.getCapacity(), r.getDescription(), r.isActive(),
                r.getBuilding(),
                r.getRoomType() != null ? r.getRoomType().name() : null,
                fac);
    }
}
