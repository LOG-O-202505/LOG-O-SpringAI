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
    private Long travelId;  // ✅ Travel이 삭제된 경우 null일 수 있음
    private String name;
    private String url;

    public static TravelImageDto fromEntity(TravelImage travelImage) {
        return TravelImageDto.builder()
                .tiuid(travelImage.getTiuid())
                .userId(travelImage.getUser().getUuid())
                // ✅ Travel이 null일 수 있으므로 null 체크 추가
                .travelId(travelImage.getTravel() != null ? travelImage.getTravel().getTuid() : null)
                .name(travelImage.getName())
                .url(travelImage.getUrl())
                .build();
    }

    public TravelImage toEntity(User user, Travel travel) {
        return TravelImage.builder()
                .user(user)
                .travel(travel)  // ✅ travel이 null일 수 있음
                .name(name)
                .url(url)
                .build();
    }
}