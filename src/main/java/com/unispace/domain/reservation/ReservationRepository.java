package com.unispace.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdOrderByStartTimeDesc(Long userId);

    /**
     * 같은 방에서 시간대가 겹치는(active) 예약을 조회 → 중복 대여 충돌 방지.
     * 겹침 조건: 기존.start < 신규.end  AND  기존.end > 신규.start
     */
    @Query("""
        select r from Reservation r
        where r.room.id = :roomId
          and r.status = com.unispace.domain.reservation.ReservationStatus.RESERVED
          and r.startTime < :endTime
          and r.endTime   > :startTime
    """)
    List<Reservation> findOverlapping(@Param("roomId") Long roomId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /** 특정 방의 특정 날짜 범위 예약 (대시보드 / 시간대 현황용) */
    @Query("""
        select r from Reservation r
        where r.room.id = :roomId
          and r.status = com.unispace.domain.reservation.ReservationStatus.RESERVED
          and r.startTime < :rangeEnd
          and r.endTime   > :rangeStart
        order by r.startTime asc
    """)
    List<Reservation> findActiveInRange(@Param("roomId") Long roomId,
                                        @Param("rangeStart") LocalDateTime rangeStart,
                                        @Param("rangeEnd") LocalDateTime rangeEnd);

    List<Reservation> findByStatusOrderByStartTimeAsc(ReservationStatus status);
}
