package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import com.ssafy.logoserver.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 여행 지역 DTO
 * 여행 지역 정보 전송을 위한 데이터 전송 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelAreaDto {
    /**
     * 여행 지역 고유 ID
     */
    private Long tauid;

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 지역 ID (시/도, 시/군/구 정보)
     */
    private Long areaId;

    /**
     * 여행 ID
     */
    private Long travelId;

    /**
     * 여행 일차 ID
     */
    private Long travelDayId;

    /**
     * 장소 ID
     */
    private Long placeId;

    /**
     * 방문 시작 시간
     */
    private LocalDateTime startTime;

    /**
     * 메모
     */
    private String memo;

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
                .placeId(travelArea.getPlace() != null ? travelArea.getPlace().getPuid() : null) // null 체크 추가
                .startTime(travelArea.getStartTime())
                .memo(travelArea.getMemo())
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