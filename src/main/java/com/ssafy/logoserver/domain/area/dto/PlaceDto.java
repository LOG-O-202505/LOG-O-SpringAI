package com.ssafy.logoserver.domain.area.dto;

import com.ssafy.logoserver.domain.area.entity.Place;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "장소 DTO")
public class PlaceDto {

    @Schema(description = "장소 ID", example = "1")
    private Long puid;

    @Schema(description = "장소 주소", example = "제주특별자치도 서귀포시 성산읍 일출로 284-12")
    private String address;

    @Schema(description = "지역 ID", example = "1")
    private Long areaId;

    @Schema(description = "장소 이름", example = "성산일출봉")
    private String name;

    @Schema(description = "위도", example = "33.459198")
    private Double latitude;

    @Schema(description = "경도", example = "126.942394")
    private Double longitude;

    public static PlaceDto fromEntity(Place place) {
        return PlaceDto.builder()
                .puid(place.getPk().getPuid())
                .address(place.getPk().getAddress())
                .areaId(place.getArea() != null ? place.getArea().getAuid() : null)
                .name(place.getName())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .build();
    }
}