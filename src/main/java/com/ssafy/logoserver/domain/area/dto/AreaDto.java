package com.ssafy.logoserver.domain.area.dto;

import com.ssafy.logoserver.domain.area.entity.Area;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "지역 DTO")
public class AreaDto {

    @Schema(description = "지역 ID", example = "1")
    private Long auid;

    @Schema(description = "지역 이름", example = "서울", required = true)
    private String areaName;

    public static AreaDto fromEntity(Area area) {
        return AreaDto.builder()
                .auid(area.getAuid())
                .areaName(area.getAreaName())
                .build();
    }
}