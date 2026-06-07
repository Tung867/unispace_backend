package com.unispace.controller;

import com.unispace.domain.user.Role;
import com.unispace.dto.request.FacilityRequest;
import com.unispace.dto.request.RoleUpdateRequest;
import com.unispace.dto.request.RoomRequest;
import com.unispace.dto.response.*;
import com.unispace.service.GoogleDriveBackupService;
import com.unispace.service.ReservationService;
import com.unispace.service.RoomService;
import com.unispace.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin", description = "관리자 전용 (공간/시설/사용자/예약/백업)")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RoomService roomService;
    private final UserService userService;
    private final ReservationService reservationService;
    private final GoogleDriveBackupService backupService;

    public AdminController(RoomService roomService, UserService userService,
                           ReservationService reservationService,
                           GoogleDriveBackupService backupService) {
        this.roomService = roomService;
        this.userService = userService;
        this.reservationService = reservationService;
        this.backupService = backupService;
    }

    // ----- 공간 관리 -----
    @Operation(summary = "공간 생성")
    @PostMapping("/rooms")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(req));
    }

    @Operation(summary = "공간 수정 (시설 편집 포함)")
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,
                                                   @RequestBody RoomRequest req) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, req));
    }

    @Operation(summary = "공간 삭제")
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    // ----- 시설 관리 -----
    @Operation(summary = "시설 생성 (모니터, 빔 프로젝터 등)")
    @PostMapping("/facilities")
    public ResponseEntity<FacilityResponse> createFacility(@Valid @RequestBody FacilityRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createFacility(req));
    }

    @Operation(summary = "시설 목록")
    @GetMapping("/facilities")
    public ResponseEntity<List<FacilityResponse>> facilities() {
        return ResponseEntity.ok(roomService.getAllFacilities());
    }

    // ----- 사용자 관리 -----
    @Operation(summary = "전체 사용자 조회")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> users() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "사용자 권한 변경")
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable Long userId,
                                                   @Valid @RequestBody RoleUpdateRequest req) {
        return ResponseEntity.ok(userService.changeRole(userId, req.role()));
    }

    // ----- 예약 관리 -----
    @Operation(summary = "전체 활성 예약 조회 (대시보드)")
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> reservations() {
        return ResponseEntity.ok(reservationService.getAllActive());
    }

    @Operation(summary = "예약 강제 취소")
    @PostMapping("/reservations/{reservationId}/force-cancel")
    public ResponseEntity<ReservationResponse> forceCancel(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.forceCancel(reservationId));
    }

    // ----- 백업 (Google Drive) -----
    @Operation(summary = "예약 내역 Google Drive 백업")
    @PostMapping("/backup")
    public ResponseEntity<Map<String, String>> backup() {
        String result = backupService.backupReservations();
        return ResponseEntity.ok(Map.of("result", result));
    }
}
