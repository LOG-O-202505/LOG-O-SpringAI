package com.ssafy.logoserver.domain.travel.dto;

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
 * 각 여행 지역에 장소 정보와 인증 정보가 포함되어 전송됨
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

    /**
     * 여행 지역 목록 (각 지역에 장소 정보와 인증 정보가 포함됨)
     */
    @Schema(description = "여행 지역 목록 (장소 정보와 인증 정보 포함)")
    private List<TravelAreaDto> travelAreas;

    /**
     * 엔티티에서 상세 DTO 변환 (기존 places, verifications 파라미터 제거)
     * @param travelRoot 여행 루트 엔티티
     * @param travelAreas 여행 지역 DTO 리스트 (각각에 장소 정보와 인증 정보가 포함됨)
     * @return 변환된 상세 DTO
     */
    public static TravelRootDetailDto fromEntity(TravelRoot travelRoot, List<TravelAreaDto> travelAreas) {
        return TravelRootDetailDto.builder()
                .truid(travelRoot.getTruid())
                .travelId(travelRoot.getTravel().getTuid())
                .day(travelRoot.getDay())
                .travelDate(travelRoot.getTravelDate())
                .travelAreas(travelAreas) // 각 TravelArea에 장소 정보와 인증 정보가 포함되어 있음
                .build();
    }
}