package com.spaceres.service;

import com.spaceres.dto.request.ReservationRequest;
import com.spaceres.dto.response.ReservationResponse;
import com.spaceres.entity.Reservation;
import com.spaceres.entity.Space;
import com.spaceres.entity.User;
import com.spaceres.exception.BusinessException;
import com.spaceres.exception.ErrorCode;
import com.spaceres.repository.ReservationRepository;
import com.spaceres.repository.SpaceRepository;
import com.spaceres.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 예약 서비스 - Pessimistic Lock 적용
 *
 * ── 동작 흐름 ──────────────────────────────────────────────
 *  1. findByIdWithLock() → SELECT ... FOR UPDATE
 *     → 같은 spaceId에 대해 동시 요청이 오면 하나씩 순서대로 처리
 *
 *  2. findOverlappingReservations() → 시간 중복 검사
 *     → 이미 같은 시간대 예약이 있으면 예외 발생
 *
 *  3. 중복 없으면 예약 저장 → 트랜잭션 종료 → Lock 해제
 * ──────────────────────────────────────────────────────────
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;

    // ── 예약 생성 (핵심: Pessimistic Lock) ───────────────
    @Transactional
    public ReservationResponse createReservation(String userEmail, ReservationRequest request) {

        // 1. 시간 유효성 검사
        validateTime(request.getStartTime(), request.getEndTime());

        // 2. 공간 조회 + Pessimistic Lock 획득
        //    → SELECT * FROM spaces WHERE id = ? FOR UPDATE
        //    → 이 트랜잭션이 끝날 때까지 다른 트랜잭션은 대기
        Space space = spaceRepository.findByIdWithLock(request.getSpaceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SPACE_NOT_FOUND));

        // 3. 공간 사용 가능 여부 확인
        if (space.getStatus() == Space.SpaceStatus.UNAVAILABLE) {
            throw new BusinessException(ErrorCode.SPACE_UNAVAILABLE);
        }

        // 4. 시간 중복 검사
        //    → Lock을 잡은 상태에서 검사하므로 동시 요청도 안전
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(
                space.getId(), request.getStartTime(), request.getEndTime());

        if (!overlapping.isEmpty()) {
            log.warn("예약 충돌 감지 - space: {}, 요청: {} ~ {}",
                    space.getName(), request.getStartTime(), request.getEndTime());
            throw new BusinessException(ErrorCode.RESERVATION_CONFLICT);
        }

        // 5. 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 6. 예약 저장
        Reservation reservation = Reservation.builder()
                .user(user)
                .space(space)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .purpose(request.getPurpose())
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("예약 생성 완료 - ID: {}, 공간: {}, 사용자: {}",
                saved.getId(), space.getName(), userEmail);

        return ReservationResponse.from(saved);
    }

    // ── 예약 취소 ─────────────────────────────────────────
    @Transactional
    public void cancelReservation(Long reservationId, String userEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        // 본인 예약인지 확인 (관리자는 모두 취소 가능 → Controller에서 @PreAuthorize로 처리)
        if (!reservation.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.ALREADY_CANCELLED);
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        log.info("예약 취소 - ID: {}, 사용자: {}", reservationId, userEmail);
    }

    // ── 내 예약 목록 ──────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return reservationRepository.findByUserIdOrderByStartTimeDesc(user.getId())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    // ── 공간별 예약 현황 (대시보드용) ─────────────────────
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUpcomingBySpace(Long spaceId) {
        return reservationRepository.findUpcomingBySpaceId(spaceId, LocalDateTime.now())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    // ── 시간 유효성 검사 ──────────────────────────────────
    private static final LocalTime OPEN_TIME  = LocalTime.of(9, 0);   // 오전 9시
    private static final LocalTime CLOSE_TIME = LocalTime.of(21, 0);  // 오후 9시
    private static final long MAX_DURATION_HOURS = 2;                 // 최대 2시간

    private void validateTime(LocalDateTime start, LocalDateTime end) {

        // 1. 종료 시간이 시작 시간 이후인지 확인
        if (!end.isAfter(start)) {
            throw new BusinessException(ErrorCode.INVALID_TIME_RANGE);
        }

        // 2. 운영 시간 확인: 시작 시간 09:00 이상, 종료 시간 21:00 이하
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime   = end.toLocalTime();

        if (startTime.isBefore(OPEN_TIME) || startTime.isAfter(CLOSE_TIME)) {
            throw new BusinessException(ErrorCode.OUTSIDE_OPERATING_HOURS);
        }
        if (endTime.isBefore(OPEN_TIME) || endTime.isAfter(CLOSE_TIME)) {
            throw new BusinessException(ErrorCode.OUTSIDE_OPERATING_HOURS);
        }

        // 3. 최대 예약 시간 확인: 2시간 초과 불가
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes > MAX_DURATION_HOURS * 60) {
            throw new BusinessException(ErrorCode.EXCEEDS_MAX_DURATION);
        }
    }
}
