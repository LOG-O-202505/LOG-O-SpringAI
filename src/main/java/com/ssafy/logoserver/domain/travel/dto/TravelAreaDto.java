package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import com.ssafy.logoserver.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelAreaDto {
    private Long tauid;
    private Long userId;
    private Long areaId;
    private Long travelId;
    private Long travelDayId;
    private Integer seq;
    private LocalDateTime startTime;
    private String memo;

    public static TravelAreaDto fromEntity(TravelArea travelArea) {
        return TravelAreaDto.builder()
                .tauid(travelArea.getTauid())
                .userId(travelArea.getUser().getUuid())
                .areaId(travelArea.getArea().getAuid())
                .travelId(travelArea.getTravel().getTuid())
                .travelDayId(travelArea.getTravelDay().getTruid())
                .seq(travelArea.getSeq())
                .startTime(travelArea.getStartTime())
                .memo(travelArea.getMemo())
                .build();
    }

    public TravelArea toEntity(User user, Area area, Travel travel, TravelRoot travelDay) {
        return TravelArea.builder()
                .user(user)
                .area(area)
                .travel(travel)
                .travelDay(travelDay)
                .seq(seq)
                .startTime(startTime)
                .memo(memo)
                .build();
    }
}