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
            Facility monitor = facilityRepository.save(Facility.builder().name("모니터").build());
            Facility beam = facilityRepository.save(Facility.builder().name("빔 프로젝터").build());
            Facility whiteboard = facilityRepository.save(Facility.builder().name("화이트보드").build());

            roomRepository.save(Room.builder()
                    .name("IT 2호관 독서실")
                    .location("오도 IT 2호관 3층")
                    .capacity(4)
                    .description("개인 학습용 독서실 좌석")
                    .active(true)
                    .facilities(Set.of(monitor))
                    .build());

            roomRepository.save(Room.builder()
                    .name("그룹 스터디룸 A")
                    .location("복지관 2층")
                    .capacity(6)
                    .description("팀 프로젝트 회의용 룸")
                    .active(true)
                    .facilities(Set.of(monitor, beam, whiteboard))
                    .build());

            log.info("[Init] 데모용 공간/시설 생성 완료");
        }
    }
}
