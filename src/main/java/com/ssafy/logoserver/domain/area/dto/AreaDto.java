package com.ssafy.logoserver.domain.area.dto;

import com.ssafy.logoserver.domain.area.entity.Area;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaDto {
    private Long auid;
    private String areaName;

    public static AreaDto fromEntity(Area area) {
        return AreaDto.builder()
                .auid(area.getAuid())
                .areaName(area.getAreaName())
                .build();
    }
}