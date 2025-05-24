package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.area.dto.PlaceDto;
import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import com.ssafy.logoserver.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 여행 지역 DTO
 * 여행 지역 정보 전송을 위한 데이터 전송 객체
 * 장소 정보와 인증 정보를 포함하여 전송
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여행 지역 DTO")
public class TravelAreaDto {
    /**
     * 여행 지역 고유 ID
     */
    @Schema(description = "여행 지역 ID", example = "1")
    private Long tauid;

    /**
     * 사용자 ID
     */
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    /**
     * 지역 ID (시/도, 시/군/구 정보)
     */
    @Schema(description = "지역 ID", example = "23")
    private Long areaId;

    /**
     * 여행 ID
     */
    @Schema(description = "여행 ID", example = "1")
    private Long travelId;

    /**
     * 여행 일차 ID
     */
    @Schema(description = "여행 루트 ID", example = "1")
    private Long travelDayId;

    /**
     * 장소 정보 (기존 placeId 대신 전체 장소 정보를 포함)
     */
    @Schema(description = "장소 정보")
    private PlaceDto place;

    /**
     * 방문 시작 시간
     */
    @Schema(description = "방문 시작 시간")
    private LocalDateTime startTime;

    /**
     * 메모
     */
    @Schema(description = "메모", example = "맛집 탐방")
    private String memo;

    /**
     * 해당 장소의 인증 정보 목록
     */
    @Schema(description = "인증 정보 목록")
    private List<VerificationDto> verifications;

    /**
     * 엔티티를 DTO로 변환하는 정적 메서드
     * @param travelArea 여행 지역 엔티티
     * @return 변환된 DTO
     */
    public static TravelAreaDto fromEntity(TravelArea travelArea) {
        return TravelAreaDto.builder()
                .tauid(travelArea.getTauid())
                .userId(travelArea.getUser().getUuid())
                .areaId(travelArea.getArea().getAuid())
                .travelId(travelArea.getTravel().getTuid())
                .travelDayId(travelArea.getTravelDay().getTruid())
                .place(travelArea.getPlace() != null ? PlaceDto.fromEntity(travelArea.getPlace()) : null) // Place 엔티티를 PlaceDto로 변환
                .startTime(travelArea.getStartTime())
                .memo(travelArea.getMemo())
                .build();
    }

    /**
     * 엔티티를 DTO로 변환하는 정적 메서드 (인증 정보 포함)
     * @param travelArea 여행 지역 엔티티
     * @param verifications 해당 장소의 인증 정보 목록
     * @return 변환된 DTO
     */
    public static TravelAreaDto fromEntityWithVerifications(TravelArea travelArea, List<VerificationDto> verifications) {
        return TravelAreaDto.builder()
                .tauid(travelArea.getTauid())
                .userId(travelArea.getUser().getUuid())
                .areaId(travelArea.getArea().getAuid())
                .travelId(travelArea.getTravel().getTuid())
                .travelDayId(travelArea.getTravelDay().getTruid())
                .place(travelArea.getPlace() != null ? PlaceDto.fromEntity(travelArea.getPlace()) : null) // Place 엔티티를 PlaceDto로 변환
                .startTime(travelArea.getStartTime())
                .memo(travelArea.getMemo())
                .verifications(verifications) // 인증 정보 목록 추가
                .build();
    }

    /**
     * DTO를 엔티티로 변환하는 메서드
     * @param user 사용자 엔티티
     * @param area 지역 엔티티
     * @param travel 여행 엔티티
     * @param travelDay 여행 일차 엔티티
     * @param place 장소 엔티티 (nullable)
     * @return 변환된 엔티티
     */
    public TravelArea toEntity(User user, Area area, Travel travel, TravelRoot travelDay, Place place) {
        return TravelArea.builder()
                .user(user)
                .area(area)
                .travel(travel)
                .travelDay(travelDay)
                .place(place)
                .startTime(startTime)
                .memo(memo)
                .build();
    }
}