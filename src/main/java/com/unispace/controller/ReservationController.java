package com.unispace.controller;

import com.unispace.dto.request.ReservationRequest;
import com.unispace.dto.response.ReservationResponse;
import com.unispace.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Reservation", description = "예약 / 반납 / 조회 (중복 충돌 방지)")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(summary = "공간 예약 (중복 시간대 충돌 방지)")
    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(Principal principal,
                                                       @Valid @RequestBody ReservationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.reserve(principal.getName(), req));
    }

    @Operation(summary = "반납")
    @PostMapping("/{reservationId}/return")
    public ResponseEntity<ReservationResponse> returnReservation(Principal principal,
                                                                 @PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.returnReservation(principal.getName(), reservationId));
    }

    @Operation(summary = "예약 취소")
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> cancel(Principal principal,
                                                      @PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.cancel(principal.getName(), reservationId));
    }

    @Operation(summary = "내 예약 목록")
    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> myReservations(Principal principal) {
        return ResponseEntity.ok(reservationService.myReservations(principal.getName()));
    }
}
