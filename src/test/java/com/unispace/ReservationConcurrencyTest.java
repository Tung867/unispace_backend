package com.unispace;

import com.unispace.domain.reservation.ReservationRepository;
import com.unispace.domain.reservation.ReservationStatus;
import com.unispace.domain.room.Room;
import com.unispace.domain.room.RoomRepository;
import com.unispace.domain.user.Role;
import com.unispace.domain.user.User;
import com.unispace.domain.user.UserRepository;
import com.unispace.dto.request.ReservationRequest;
import com.unispace.exception.BusinessException;
import com.unispace.exception.ErrorCode;
import com.unispace.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시성 테스트 (중복 대여 충돌 방지 - Lock 검증).
 * 동일 공간 / 동일 시간대로 여러 스레드가 "동시에" 예약을 시도했을 때,
 * 정확히 1건만 성공하고 나머지는 충돌(409)로 거부되는지 검증한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class ReservationConcurrencyTest {

    private static final int THREADS = 8;

    @Autowired private ReservationService reservationService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long roomId;
    private final List<String> usernames = new ArrayList<>();

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        roomRepository.deleteAll();

        Room room = roomRepository.save(Room.builder()
                .name("동시성 테스트룸")
                .location("test")
                .capacity(1)
                .active(true)
                .build());
        roomId = room.getId();

        usernames.clear();
        for (int i = 0; i < THREADS; i++) {
            String username = "concurrent_user_" + i;
            userRepository.save(User.builder()
                    .username(username)
                    .password(passwordEncoder.encode("test1234"))
                    .name("동시성 사용자 " + i)
                    .role(Role.USER)
                    .build());
            usernames.add(username);
        }
    }

    @Test
    @DisplayName("동일 시간대 동시 예약 시 단 1건만 성공한다")
    void onlyOneReservationSucceedsForSameSlot() throws InterruptedException {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = start.plusHours(2);
        ReservationRequest request = new ReservationRequest(roomId, start, end);

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch readyLatch = new CountDownLatch(THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREADS);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();

        for (String username : usernames) {
            pool.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();          // 모든 스레드가 동시에 출발
                    reservationService.reserve(username, request);
                    success.incrementAndGet();
                } catch (BusinessException e) {
                    if (e.getErrorCode() == ErrorCode.RESERVATION_CONFLICT) {
                        conflict.incrementAndGet();
                    } else {
                        other.incrementAndGet();
                    }
                } catch (Exception e) {
                    other.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();           // 모든 스레드 준비 완료 대기
        startLatch.countDown();       // 동시 출발 신호
        doneLatch.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        long reservedInDb = reservationRepository
                .findByStatusOrderByStartTimeAsc(ReservationStatus.RESERVED).size();

        System.out.printf("[동시성 결과] 성공=%d, 충돌=%d, 기타=%d, DB의 RESERVED=%d%n",
                success.get(), conflict.get(), other.get(), reservedInDb);

        // 핵심 불변식: 더블 부킹이 발생하지 않는다.
        assertThat(success.get()).isEqualTo(1);
        assertThat(reservedInDb).isEqualTo(1);
        assertThat(success.get() + conflict.get() + other.get()).isEqualTo(THREADS);
    }

    @Test
    @DisplayName("겹치지 않는 시간대는 모두 성공한다")
    void nonOverlappingReservationsAllSucceed() {
        LocalDateTime base = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);

        for (int i = 0; i < THREADS; i++) {
            LocalDateTime start = base.plusHours(i);   // 09-10, 10-11, ...
            LocalDateTime end = start.plusHours(1);
            reservationService.reserve(usernames.get(i), new ReservationRequest(roomId, start, end));
        }

        long reserved = reservationRepository
                .findByStatusOrderByStartTimeAsc(ReservationStatus.RESERVED).size();
        assertThat(reserved).isEqualTo(THREADS);
    }
}
