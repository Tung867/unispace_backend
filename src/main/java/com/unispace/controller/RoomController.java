package com.unispace.controller;

import com.unispace.dto.response.RoomAvailabilityResponse;
import com.unispace.dto.response.RoomResponse;
import com.unispace.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Room", description = "공간 조회 / 시간대·시설 현황")
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(summary = "예약 가능한 공간 목록")
    @GetMapping
    public ResponseEntity<List<RoomResponse>> list() {
        return ResponseEntity.ok(roomService.getActiveRooms());
    }

    @Operation(summary = "공간 상세")
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> detail(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoom(roomId));
    }

    @Operation(summary = "공간 시설 + 점유 시간대 현황 (대시보드)")
    @GetMapping("/{roomId}/availability")
    public ResponseEntity<RoomAvailabilityResponse> availability(
            @PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(roomService.getAvailability(roomId, from, to));
    }
}
