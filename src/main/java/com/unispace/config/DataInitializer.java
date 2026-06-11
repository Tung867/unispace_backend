package com.unispace.config;

import com.unispace.domain.room.Facility;
import com.unispace.domain.room.FacilityRepository;
import com.unispace.domain.room.Room;
import com.unispace.domain.room.RoomRepository;
import com.unispace.domain.user.Role;
import com.unispace.domain.user.User;
import com.unispace.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/** 최초 실행 시 관리자 계정과 데모용 공간/시설을 생성 (없을 때만). */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final FacilityRepository facilityRepository;
    private final PasswordEncoder passwordEncoder;

    private final String adminUsername;
    private final String adminPassword;
    private final String adminName;

    public DataInitializer(UserRepository userRepository,
                           RoomRepository roomRepository,
                           FacilityRepository facilityRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${unispace.admin.username:admin}") String adminUsername,
                           @Value("${unispace.admin.password:admin1234}") String adminPassword,
                           @Value("${unispace.admin.name:관리자}") String adminName) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.facilityRepository = facilityRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername(adminUsername)) {
            userRepository.save(User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .name(adminName)
                    .email("admin@unispace.local")
                    .affiliation("System")
                    .role(Role.ADMIN)
                    .build());
            log.info("[Init] 관리자 계정 생성: {} / {}", adminUsername, adminPassword);
        }

        if (roomRepository.count() == 0) {
            Facility monitor    = facilityRepository.save(Facility.builder().name("모니터").build());
            Facility beam       = facilityRepository.save(Facility.builder().name("빔 프로젝터").build());
            Facility whiteboard = facilityRepository.save(Facility.builder().name("화이트보드").build());

            // 공학관 (engineering) — 이름에 "공학" 포함
            saveRoom("공학관 세미나실 A", "공학관 401호", 8, "팀 미팅·세미나용 회의실", Set.of(beam, whiteboard));
            saveRoom("공학관 개인 좌석",  "공학관 IT 2호관 3층", 1, "집중 학습용 개인 좌석", Set.of(monitor));

            // 도서관 (library) — 이름에 "도서관" 포함
            saveRoom("도서관 그룹 스터디룸", "중앙도서관 5층", 6, "그룹 스터디 전용 룸", Set.of(monitor, whiteboard));
            saveRoom("도서관 개인 열람석",   "중앙도서관 3층", 1, "조용한 집중 학습 좌석", Set.of(monitor));

            // 학생회관 (student-union) — 이름에 "학생" 포함
            saveRoom("학생회관 동아리 회의실", "학생회관 2층", 10, "동아리·소모임 회의 공간", Set.of(beam));
            saveRoom("학생회관 개인 부스",     "학생회관 1층", 1, "1인 집중 부스", Set.of(monitor));

            // 인문관 (liberal-arts) — 이름에 "인문" 포함
            saveRoom("인문관 토론실", "인문관 305호", 12, "세미나·토론용 강의실", Set.of(beam));

            // 자연과학관 (science) — 이름에 "과학" 포함
            saveRoom("자연과학관 집중 좌석", "자연과학관 2층", 1, "개인 집중 학습 좌석", Set.of(monitor));

            log.info("[Init] 데모용 공간/시설 생성 완료 (총 {}개)", roomRepository.count());
        }
    }

    private void saveRoom(String name, String location, int capacity, String description, Set<Facility> facilities) {
        roomRepository.save(Room.builder()
                .name(name)
                .location(location)
                .capacity(capacity)
                .description(description)
                .active(true)
                .facilities(facilities)
                .build());
    }
}
