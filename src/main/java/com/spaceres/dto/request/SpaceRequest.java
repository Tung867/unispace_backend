package com.spaceres.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpaceRequest {

    @NotBlank(message = "공간 이름을 입력해주세요")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @Min(value = 1, message = "수용 인원은 1명 이상이어야 합니다")
    private int capacity;

    @Size(max = 200)
    private String location;

    @Size(max = 500)
    private String facilities;

    @Size(max = 100)
    private String building;

    private com.spaceres.entity.Space.SpaceType spaceType;
}
