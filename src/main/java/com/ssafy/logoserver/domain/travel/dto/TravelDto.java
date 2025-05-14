package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelDto {
    private Long tuid;
    private Long userId;
    private String location;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer peoples;
    private String season;
    private LocalDateTime created;

    public static TravelDto fromEntity(Travel travel) {
        return TravelDto.builder()
                .tuid(travel.getTuid())
                .userId(travel.getUser().getUuid())
                .location(travel.getLocation())
                .title(travel.getTitle())
                .startDate(travel.getStartDate())
                .endDate(travel.getEndDate())
                .peoples(travel.getPeoples())
                .season(travel.getSeason())
                .created(travel.getCreated())
                .build();
    }

    public Travel toEntity(User user) {
        return Travel.builder()
                .user(user)
                .location(location)
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
                .peoples(peoples)
                .season(season)
                .build();
    }
}