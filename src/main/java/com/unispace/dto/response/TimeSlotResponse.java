package com.unispace.dto.response;

import java.time.LocalDateTime;

/** 대시보드: 한 공간의 점유된 시간대 한 칸 */
public record TimeSlotResponse(
        Long reservationId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reservedBy
) {}
