package com.unispace.domain.room;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/** 예약 가능한 공간 */
@Entity
@Table(name = "rooms")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** 위치 (예: IT 2호관 독서실) */
    @Column(length = 150)
    private String location;

    private Integer capacity;

    @Column(length = 500)
    private String description;

    /** 건물명 (예: 공학관, 도서관) */
    @Column(length = 100)
    private String building;

    /** 공간 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", length = 50)
    private RoomType roomType;

    /** 예약 가능 여부 (Admin 이 비활성화 가능) */
    @Column(nullable = false)
    private boolean active;

    public enum RoomType {
        MEETING_ROOM,
        INDIVIDUAL_SEAT
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "room_facilities",
        joinColumns = @JoinColumn(name = "room_id"),
        inverseJoinColumns = @JoinColumn(name = "facility_id")
    )
    @Builder.Default
    private Set<Facility> facilities = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
