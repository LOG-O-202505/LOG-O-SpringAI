package com.ssafy.logoserver.domain.image.dto;

import com.ssafy.logoserver.domain.image.entity.TravelImage;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelImageDto {
    private Long tiuid;
    private Long userId;
    private Long travelId;
    private String name;
    private String url;

    public static TravelImageDto fromEntity(TravelImage travelImage) {
        return TravelImageDto.builder()
                .tiuid(travelImage.getTiuid())
                .userId(travelImage.getUser().getUuid())
                .travelId(travelImage.getTravel().getTuid())
                .name(travelImage.getName())
                .url(travelImage.getUrl())
                .build();
    }

    public TravelImage toEntity(User user, Travel travel) {
        return TravelImage.builder()
                .user(user)
                .travel(travel)
                .name(name)
                .url(url)
                .build();
    }
}