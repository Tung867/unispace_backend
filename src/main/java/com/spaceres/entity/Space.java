package com.spaceres.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 공간(Space) 엔티티
 * - 관리자가 생성/편집
 * - 시설 목록 포함
 */
@Entity
@Table(name = "spaces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // 최대 수용 인원
    @Column(nullable = false)
    private int capacity;

    // 위치 (예: 3층 회의실A)
    @Column(length = 200)
    private String location;

    // 시설 목록 (프로젝터, 화이트보드 등) - JSON 문자열로 저장
    @Column(length = 500)
    private String facilities;

    // 건물 ID (예: engineering, library, student-union 등)
    @Column(length = 100)
    private String building;

    // 공간 유형 (소형 회의실 / 개인 좌석)
    @Enumerated(EnumType.STRING)
    @Column(name = "space_type", length = 50)
    private SpaceType spaceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SpaceStatus status = SpaceStatus.AVAILABLE;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum SpaceStatus {
        AVAILABLE,   // 사용 가능
        UNAVAILABLE  // 사용 불가 (점검 등)
    }

    public enum SpaceType {
        MEETING_ROOM,     // 소형 회의실
        INDIVIDUAL_SEAT   // 개인 좌석
    }
}
