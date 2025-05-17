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

    @Schema(description = "시/도", example = "서울특별시", required = true)
    private String region;

    @Schema(description = "시/군/구", example = "성남시 중원구", required = true)
    private String sig;

    public static AreaDto fromEntity(Area area) {
        return AreaDto.builder()
                .auid(area.getAuid())
                .region(area.getRegion())
                .sig(area.getSig())
                .build();
    }
}