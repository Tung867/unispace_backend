-- =====================================================================
-- UniSpace 공간(rooms) 시드 데이터
--   - 프론트 CAMPUS_BUILDINGS 의 ID 를 rooms.building 으로 사용
--     (engineering / library / student-union / liberal-arts / science)
--   - room_type: MEETING_ROOM | INDIVIDUAL_SEAT
--   - room_facilities 매핑은 INSERT ... SELECT 로 facilities.name 매칭
--
-- 사전 조건:
--   - facilities 테이블에 아래 이름이 존재해야 함
--     (DataInitializer 또는 schema.sql 시드로 자동 생성됨)
--       모니터 / 빔 프로젝터 / 화이트보드
--   - 추가 시설이 필요하면 먼저 facilities 에 INSERT IGNORE
-- =====================================================================

-- 부족할 수 있는 시설 보충 (이미 있으면 무시)
INSERT IGNORE INTO facilities (name) VALUES
    ('모니터'),
    ('빔 프로젝터'),
    ('화이트보드'),
    ('화상회의'),
    ('콘센트'),
    ('와이파이');


-- =====================================================================
-- 1) 공학관 (engineering)
-- =====================================================================
INSERT INTO rooms (name, location, capacity, description, building, room_type, active, created_at) VALUES
('공학관 세미나실 A',     '공학관 3층 301호', 8,  '팀 프로젝트 발표용 세미나실',              'engineering', 'MEETING_ROOM',     b'1', NOW(6)),
('공학관 회의실 B',       '공학관 4층 402호', 6,  '소규모 회의 / 코드 리뷰 공간',             'engineering', 'MEETING_ROOM',     b'1', NOW(6)),
('공학관 프로젝트룸',     '공학관 5층 501호', 10, '캡스톤 / 졸업 프로젝트 회의실',            'engineering', 'MEETING_ROOM',     b'1', NOW(6)),
('공학관 개인 학습실 1',  '공학관 2층 열람실', 1, '모니터 비치 1인용 개인 학습 좌석',         'engineering', 'INDIVIDUAL_SEAT', b'1', NOW(6)),
('공학관 개인 학습실 2',  '공학관 2층 열람실', 1, '집중 학습용 독립 좌석',                    'engineering', 'INDIVIDUAL_SEAT', b'1', NOW(6));


-- =====================================================================
-- 2) 도서관 (library)
-- =====================================================================
INSERT INTO rooms (name, location, capacity, description, building, room_type, active, created_at) VALUES
('도서관 그룹 스터디룸 1', '도서관 3층',       4, '소규모 스터디 / 토론용 룸',                'library',     'MEETING_ROOM',     b'1', NOW(6)),
('도서관 그룹 스터디룸 2', '도서관 3층',       6, '중규모 팀 스터디 룸 (모니터 비치)',         'library',     'MEETING_ROOM',     b'1', NOW(6)),
('도서관 열람석 A',       '도서관 1층 열람실', 1, '조용한 개인 열람 좌석',                    'library',     'INDIVIDUAL_SEAT', b'1', NOW(6)),
('도서관 열람석 B',       '도서관 1층 열람실', 1, '조용한 개인 열람 좌석',                    'library',     'INDIVIDUAL_SEAT', b'1', NOW(6)),
('도서관 캐럴 좌석',      '도서관 2층',       1, '칸막이형 1인용 캐럴 (모니터)',              'library',     'INDIVIDUAL_SEAT', b'1', NOW(6));


-- =====================================================================
-- 3) 학생회관 (student-union)
-- =====================================================================
INSERT INTO rooms (name, location, capacity, description, building, room_type, active, created_at) VALUES
('학생회관 동아리방 1',   '학생회관 2층 201호', 8, '동아리 / 소모임 회의 공간',                'student-union', 'MEETING_ROOM',     b'1', NOW(6)),
('학생회관 소모임실',     '학생회관 3층 305호', 6, '발표 연습 / 소모임 회의용',                'student-union', 'MEETING_ROOM',     b'1', NOW(6)),
('학생회관 휴게 좌석',    '학생회관 1층 라운지', 1, '개방형 1인 좌석 (와이파이 / 콘센트)',     'student-union', 'INDIVIDUAL_SEAT', b'1', NOW(6));


-- =====================================================================
-- 4) 인문관 (liberal-arts)
-- =====================================================================
INSERT INTO rooms (name, location, capacity, description, building, room_type, active, created_at) VALUES
('인문관 세미나실',       '인문관 2층 210호', 10, '강독 / 세미나 / 발표 공간',                'liberal-arts', 'MEETING_ROOM',     b'1', NOW(6)),
('인문관 토론실 A',       '인문관 3층 305호', 8,  '토론 / 자유 발표용 회의실',                'liberal-arts', 'MEETING_ROOM',     b'1', NOW(6)),
('인문관 자습실 좌석',    '인문관 1층 자습실', 1,  '인문계열 학생 전용 1인 좌석',              'liberal-arts', 'INDIVIDUAL_SEAT', b'1', NOW(6));


-- =====================================================================
-- 5) 자연과학관 (science)
-- =====================================================================
INSERT INTO rooms (name, location, capacity, description, building, room_type, active, created_at) VALUES
('자연과학관 연구 세미나실', '자연과학관 4층 401호', 12, '연구실 합동 세미나 / 발표 공간',     'science',     'MEETING_ROOM',     b'1', NOW(6)),
('자연과학관 실험 회의실',   '자연과학관 3층 312호', 6,  '실험 결과 리뷰 / 미팅 공간',         'science',     'MEETING_ROOM',     b'1', NOW(6)),
('자연과학관 자율 학습실',   '자연과학관 2층 열람실', 1, '모니터 비치 1인 자율 학습 좌석',     'science',     'INDIVIDUAL_SEAT', b'1', NOW(6));


-- =====================================================================
-- 공간 ↔ 시설 매핑 (room_facilities)
--   * 동일한 이름이 여러 row 면 모두 매핑되니, 위 INSERT 가 1회만 실행됐다고 가정.
--   * 이미 매핑이 있다면 PRIMARY KEY 충돌 → INSERT IGNORE 사용
-- =====================================================================

-- 공학관 -----------------------------------------------------------
INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '공학관 세미나실 A' AND f.name IN ('모니터', '빔 프로젝터', '화이트보드');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '공학관 회의실 B' AND f.name IN ('모니터', '화이트보드');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '공학관 프로젝트룸' AND f.name IN ('빔 프로젝터', '화이트보드', '화상회의');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '공학관 개인 학습실 1' AND f.name IN ('모니터', '콘센트');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '공학관 개인 학습실 2' AND f.name IN ('콘센트');


-- 도서관 -----------------------------------------------------------
INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '도서관 그룹 스터디룸 1' AND f.name IN ('화이트보드');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '도서관 그룹 스터디룸 2' AND f.name IN ('모니터', '화이트보드');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name IN ('도서관 열람석 A', '도서관 열람석 B') AND f.name IN ('콘센트', '와이파이');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '도서관 캐럴 좌석' AND f.name IN ('모니터', '콘센트');


-- 학생회관 ---------------------------------------------------------
INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '학생회관 동아리방 1' AND f.name IN ('모니터', '화이트보드');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '학생회관 소모임실' AND f.name IN ('빔 프로젝터', '화이트보드');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '학생회관 휴게 좌석' AND f.name IN ('콘센트', '와이파이');


-- 인문관 -----------------------------------------------------------
INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '인문관 세미나실' AND f.name IN ('모니터', '빔 프로젝터');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '인문관 토론실 A' AND f.name IN ('화이트보드');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '인문관 자습실 좌석' AND f.name IN ('콘센트');


-- 자연과학관 -------------------------------------------------------
INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '자연과학관 연구 세미나실' AND f.name IN ('모니터', '빔 프로젝터', '화이트보드', '화상회의');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '자연과학관 실험 회의실' AND f.name IN ('화이트보드');

INSERT IGNORE INTO room_facilities (room_id, facility_id)
SELECT r.id, f.id FROM rooms r, facilities f
 WHERE r.name = '자연과학관 자율 학습실' AND f.name IN ('모니터', '콘센트');
