package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.area.dto.PlaceDto;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 여행 루트 상세 정보 DTO
 * 여행 루트와 관련된 모든 상세 정보를 포함하는 DTO
 */
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

    @Schema(description = "여행 일차", example = "1")
    private Integer day;

    @Schema(description = "여행 날짜", example = "2025-05-01")
    private LocalDate travelDate;

    @Schema(description = "여행 지역 목록 (장소 정보 포함)")
    private List<TravelAreaDto> travelAreas;

    @Schema(description = "장소 목록")
    private List<PlaceDto> places;

    @Schema(description = "인증 목록")
    private List<VerificationDto> verifications;

    /**
     * 엔티티에서 상세 DTO 변환
     * @param travelRoot 여행 루트 엔티티
     * @param travelAreas 여행 지역 DTO 리스트
     * @param places 장소 DTO 리스트
     * @param verifications 인증 DTO 리스트
     * @return 변환된 상세 DTO
     */
    public static TravelRootDetailDto fromEntity(TravelRoot travelRoot,
                                                 List<TravelAreaDto> travelAreas,
                                                 List<PlaceDto> places,
                                                 List<VerificationDto> verifications) {
        return TravelRootDetailDto.builder()
                .truid(travelRoot.getTruid())
                .travelId(travelRoot.getTravel().getTuid())
                .day(travelRoot.getDay())
                .travelDate(travelRoot.getTravelDate())
                .travelAreas(travelAreas) // 이제 각 TravelArea에 장소 정보가 포함됨
                .places(places)
                .verifications(verifications)
                .build();
    }
}