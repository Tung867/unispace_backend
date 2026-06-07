package com.unispace.dto.response;

import com.unispace.domain.reservation.Reservation;
import com.unispace.domain.reservation.ReservationStatus;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long roomId,
        String roomName,
        Long userId,
        String userName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ReservationStatus status,
        LocalDateTime createdAt
) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getRoom().getId(),
                r.getRoom().getName(),
                r.getUser().getId(),
                r.getUser().getName(),
                r.getStartTime(),
                r.getEndTime(),
                r.getStatus(),
                r.getCreatedAt());
    }
}
