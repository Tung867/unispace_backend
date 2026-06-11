package com.spaceres.dto.response;

import com.spaceres.entity.Space;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SpaceResponse {
    private Long id;
    private String name;
    private String description;
    private int capacity;
    private String location;
    private String facilities;
    private String status;
    private String building;
    private String spaceType;
    private LocalDateTime createdAt;

    public static SpaceResponse from(Space s) {
        return SpaceResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .capacity(s.getCapacity())
                .location(s.getLocation())
                .facilities(s.getFacilities())
                .status(s.getStatus().name())
                .building(s.getBuilding())
                .spaceType(s.getSpaceType() != null ? s.getSpaceType().name() : null)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
