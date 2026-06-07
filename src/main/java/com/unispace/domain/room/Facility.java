package com.unispace.domain.room;

import jakarta.persistence.*;
import lombok.*;

/** 편의 시설 (모니터, 빔 프로젝터 등) */
@Entity
@Table(name = "facilities")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
