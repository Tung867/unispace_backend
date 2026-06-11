package com.unispace.service;

import com.unispace.domain.reservation.Reservation;
import com.unispace.domain.reservation.ReservationRepository;
import com.unispace.domain.reservation.ReservationStatus;
import com.unispace.domain.room.Room;
import com.unispace.domain.room.RoomRepository;
import com.unispace.domain.user.User;
import com.unispace.dto.request.ReservationRequest;
import com.unispace.dto.response.ReservationResponse;
import com.unispace.exception.BusinessException;
import com.unispace.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserService userService;

    public ReservationService(ReservationRepository reservationRepository,
                              RoomRepository roomRepository,
                              UserService userService) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.userService = userService;
    }

    /**
     * 공간 예약 생성.
     * 1) Room 을 비관적 쓰기 락(PESSIMISTIC_WRITE)으로 조회 → 동시 요청 직렬화
     * 2) 겹치는 예약 검사 → 중복 대여 충돌 방지
     */
    @Transactional
    public ReservationResponse reserve(String username, ReservationRequest req) {
        validateTimeRange(req.startTime(), req.endTime());
        User user = userService.getByUsername(username);

        // 락을 걸고 방을 조회 (동시성 제어 핵심)
        Room room = roomRepository.findByIdForUpdate(req.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        if (!room.isActive()) {
            throw new BusinessException(ErrorCode.ROOM_INACTIVE);
        }

        List<Reservation> overlapping =
                reservationRepository.findOverlapping(room.getId(), req.startTime(), req.endTime());
        if (!overlapping.isEmpty()) {
            throw new BusinessException(ErrorCode.RESERVATION_CONFLICT);
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .room(room)
                .startTime(req.startTime())
                .endTime(req.endTime())
                .status(ReservationStatus.RESERVED)
                .build();
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    /** 반납 */
    @Transactional
    public ReservationResponse returnReservation(String username, Long reservationId) {
        Reservation reservation = findOwned(username, reservationId);
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED);
        }
        reservation.setStatus(ReservationStatus.RETURNED);
        reservation.setReturnedAt(LocalDateTime.now());
        return ReservationResponse.from(reservation);
    }

    /** 사용자 본인 예약 취소 */
    @Transactional
    public ReservationResponse cancel(String username, Long reservationId) {
        Reservation reservation = findOwned(username, reservationId);
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED);
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> myReservations(String username) {
        User user = userService.getByUsername(username);
        return reservationRepository.findByUserIdOrderByStartTimeDesc(user.getId())
                .stream().map(ReservationResponse::from).toList();
    }

    // ----- Admin: 강제 취소 -----
    @Transactional
    public ReservationResponse forceCancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.setStatus(ReservationStatus.CANCELLED);
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllActive() {
        return reservationRepository.findByStatusOrderByStartTimeAsc(ReservationStatus.RESERVED)
                .stream().map(ReservationResponse::from).toList();
    }

    // ----- helpers -----
    private Reservation findOwned(String username, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        if (!reservation.getUser().getUsername().equals(username)) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_OWNED);
        }
        return reservation;
    }

    private static final LocalTime OPEN_TIME  = LocalTime.of(9, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(21, 0);
    private static final long MAX_DURATION_HOURS = 2;

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start) || start.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new BusinessException(ErrorCode.INVALID_TIME_RANGE);
        }
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();
        if (startTime.isBefore(OPEN_TIME) || startTime.isAfter(CLOSE_TIME)) {
            throw new BusinessException(ErrorCode.OUTSIDE_OPERATING_HOURS);
        }
        if (endTime.isBefore(OPEN_TIME) || endTime.isAfter(CLOSE_TIME)) {
            throw new BusinessException(ErrorCode.OUTSIDE_OPERATING_HOURS);
        }
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes > MAX_DURATION_HOURS * 60) {
            throw new BusinessException(ErrorCode.EXCEEDS_MAX_DURATION);
        }
    }
}
