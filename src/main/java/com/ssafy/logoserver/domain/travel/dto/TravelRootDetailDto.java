package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.area.dto.AreaDto;
import com.ssafy.logoserver.domain.area.dto.PlaceDto;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여행 루트 상세 정보 DTO")
public class TravelRootDetailDto {

    @Schema(description = "여행 루트 ID", example = "1")
    private Long truid;

    @Schema(description = "여행 ID", example = "1")
    private Long travelId;

    @Schema(description = "지역 ID", example = "1")
    private Long areaId;

    @Schema(description = "지역 정보")
    private AreaDto area;

    @Schema(description = "여행 일차", example = "1")
    private Integer day;

    @Schema(description = "여행 날짜", example = "2025-05-01")
    private LocalDate travelDate;

    @Schema(description = "여행 지역 목록")
    private List<TravelAreaDto> travelAreas;

    @Schema(description = "장소 목록")
    private List<PlaceDto> places;

    @Schema(description = "인증 목록")
    private List<VerificationDto> verifications;

    /**
     * 엔티티에서 상세 DTO 변환
     */
    public static TravelRootDetailDto fromEntity(TravelRoot travelRoot,
                                                 AreaDto area,
                                                 List<TravelAreaDto> travelAreas,
                                                 List<PlaceDto> places,
                                                 List<VerificationDto> verifications) {
        return TravelRootDetailDto.builder()
                .truid(travelRoot.getTruid())
                .travelId(travelRoot.getTravel().getTuid())
                .areaId(travelRoot.getArea().getAuid())
                .area(area)
                .day(travelRoot.getDay())
                .travelDate(travelRoot.getTravelDate())
                .travelAreas(travelAreas)
                .places(places)
                .verifications(verifications)
                .build();
    }
}