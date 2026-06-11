-- =====================================================================
-- UniSpace 데이터베이스 스키마 (MySQL 8.x)
--   - JPA 엔티티(@Entity) 매핑과 1:1 대응
--   - 문자셋: utf8mb4 (한국어 대응)
--   - ddl-auto: update 환경에서 Hibernate 가 생성하는 결과와 동일한 구조
--
-- 적용 예:
--   mysql -u root -p
--   CREATE DATABASE IF NOT EXISTS unispace
--     DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--   USE unispace;
--   source schema.sql;
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS room_facilities;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS facilities;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- 1) users : 회원 (User.java)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    username     VARCHAR(50)  NOT NULL,
    password     VARCHAR(255) NOT NULL,
    name         VARCHAR(50)  NOT NULL,
    email        VARCHAR(100) DEFAULT NULL,
    affiliation  VARCHAR(100) DEFAULT NULL,
    role         VARCHAR(20)  NOT NULL,                  -- USER | ADMIN  (@Enumerated STRING)
    created_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 2) facilities : 편의 시설 (Facility.java)
-- ---------------------------------------------------------------------
CREATE TABLE facilities (
    id    BIGINT      NOT NULL AUTO_INCREMENT,
    name  VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_facilities_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 3) rooms : 예약 가능한 공간 (Room.java)
-- ---------------------------------------------------------------------
CREATE TABLE rooms (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100) NOT NULL,
    location     VARCHAR(150) DEFAULT NULL,
    capacity     INT          DEFAULT NULL,
    description  VARCHAR(500) DEFAULT NULL,
    building     VARCHAR(100) DEFAULT NULL,
    room_type    VARCHAR(50)  DEFAULT NULL,              -- MEETING_ROOM | INDIVIDUAL_SEAT
    active       BIT(1)       NOT NULL,                  -- @Column boolean
    created_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_rooms_active (active),
    KEY idx_rooms_building (building),
    KEY idx_rooms_room_type (room_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 4) room_facilities : Room <-> Facility (@ManyToMany JoinTable)
--      Room.facilities 에 대응
-- ---------------------------------------------------------------------
CREATE TABLE room_facilities (
    room_id      BIGINT NOT NULL,
    facility_id  BIGINT NOT NULL,
    PRIMARY KEY (room_id, facility_id),
    KEY idx_room_facilities_facility (facility_id),
    CONSTRAINT fk_room_facilities_room
        FOREIGN KEY (room_id)     REFERENCES rooms (id)      ON DELETE CASCADE,
    CONSTRAINT fk_room_facilities_facility
        FOREIGN KEY (facility_id) REFERENCES facilities (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 5) reservations : 예약 (Reservation.java)
--      - 동시 예약 충돌 방지는 Application 레벨에서
--        PESSIMISTIC_WRITE (RoomRepository#findByIdForUpdate) 로 처리
--      - 시간대 겹침 조회용 인덱스 (room_id, start_time, end_time)
-- ---------------------------------------------------------------------
CREATE TABLE reservations (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL,
    room_id      BIGINT      NOT NULL,
    start_time   DATETIME(6) NOT NULL,
    end_time     DATETIME(6) NOT NULL,
    status       VARCHAR(20) NOT NULL,                  -- RESERVED | RETURNED | CANCELLED
    created_at   DATETIME(6) NOT NULL,
    returned_at  DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_reservations_user (user_id),
    KEY idx_reservations_room_time   (room_id, start_time, end_time),
    KEY idx_reservations_status_time (status,  start_time),
    CONSTRAINT fk_reservations_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reservations_room
        FOREIGN KEY (room_id) REFERENCES rooms (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =====================================================================
-- (선택) 시드 데이터 : DataInitializer 와 동일
--   * users.password 는 BCrypt 해시. 아래 해시는 평문 "admin1234"
--     (Spring Security BCryptPasswordEncoder cost=10).
--     운영 환경에서는 반드시 교체할 것.
--   * 실제로는 DataInitializer 가 기동 시 자동 생성하므로
--     수동 SQL 시드는 보통 불필요. 필요할 때만 실행.
-- =====================================================================

-- 관리자 계정 (admin / admin1234)
INSERT INTO users (username, password, name, email, affiliation, role, created_at)
VALUES (
    'admin',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOa1qiH1jK3vY8m1Z1cQFh4xMxQ0lYQby',
    '관리자',
    'admin@unispace.local',
    'System',
    'ADMIN',
    NOW(6)
);

-- 시설
INSERT INTO facilities (name) VALUES ('모니터');
INSERT INTO facilities (name) VALUES ('빔 프로젝터');
INSERT INTO facilities (name) VALUES ('화이트보드');

-- 공간
INSERT INTO rooms (name, location, capacity, description, building, room_type, active, created_at)
VALUES ('IT 2호관 독서실', '오도 IT 2호관 3층', 4, '개인 학습용 독서실 좌석', NULL, 'INDIVIDUAL_SEAT', b'1', NOW(6));

INSERT INTO rooms (name, location, capacity, description, building, room_type, active, created_at)
VALUES ('그룹 스터디룸 A', '복지관 2층', 6, '팀 프로젝트 회의용 룸', NULL, 'MEETING_ROOM', b'1', NOW(6));

-- 공간-시설 매핑
--   1번 room (IT 2호관 독서실) ← 모니터
--   2번 room (그룹 스터디룸 A) ← 모니터, 빔 프로젝터, 화이트보드
INSERT INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = 'IT 2호관 독서실' AND f.name = '모니터';

INSERT INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '그룹 스터디룸 A' AND f.name IN ('모니터', '빔 프로젝터', '화이트보드');
