package com.ssafy.logoserver.domain.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 여행 지역 추가 요청 DTO
 * 클라이언트에서 새로운 여행 지역을 추가할 때 사용하는 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여행 지역 추가 요청 DTO")
public class TravelAreaRequestDto {

    @Schema(description = "여행 ID", example = "1")
    private Long travel_id;

    @Schema(description = "여행 루트 ID", example = "1")
    private Long travel_day_id;

    @Schema(description = "시/도", example = "1")
    private Long region;

    @Schema(description = "시/군/구", example = "1")
    private Long sig;

    @Schema(description = "장소 ID (기존 장소 선택 시)", example = "1")
    private Long place_id;

    @Schema(description = "여행지 방문 시간", example = "2023-01-01T10:00:00")
    private LocalDateTime start;

    @Schema(description = "메모", example = "맛집 탐방")
    private String memo;

    @Schema(description = "여행지 주소 (새 장소 생성 시)", example = "서울특별시 강남구 테헤란로 212")
    private String address;

    @Schema(description = "여행지 이름 (새 장소 생성 시)", example = "강남역")
    private String name;

    @Schema(description = "위도 (새 장소 생성 시)", example = "37.5012")
    private Double latitude;

    @Schema(description = "경도 (새 장소 생성 시)", example = "127.0396")
    private Double longitude;
}