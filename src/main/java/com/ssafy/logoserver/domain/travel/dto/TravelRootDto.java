package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelRootDto {
    private Long truid;
    private Long travelId;
    private Integer day;
    private LocalDate travelDate;

    public static TravelRootDto fromEntity(TravelRoot travelRoot) {
        return TravelRootDto.builder()
                .truid(travelRoot.getTruid())
                .travelId(travelRoot.getTravel().getTuid())
                .day(travelRoot.getDay())
                .travelDate(travelRoot.getTravelDate())
                .build();
    }

    public TravelRoot toEntity(Travel travel) {
        return TravelRoot.builder()
                .travel(travel)
                .day(day)
                .travelDate(travelDate)
                .build();
    }
}