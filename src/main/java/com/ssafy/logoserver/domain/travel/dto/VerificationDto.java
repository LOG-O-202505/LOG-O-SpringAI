package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.area.entity.PlacePK;
import com.ssafy.logoserver.domain.travel.entity.Verification;
import com.ssafy.logoserver.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "위치 인증 DTO")
public class VerificationDto {

    @Schema(description = "인증 ID", example = "1")
    private Long vuid;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "장소 ID", example = "1")
    private Long placeId;

    @Schema(description = "장소 주소", example = "제주특별자치도 서귀포시 성산읍 일출로 284-12")
    private String placeAddress;

    @Schema(description = "별점", example = "4.5")
    private Double star;

    @Schema(description = "리뷰", example = "너무 멋진 곳이었어요!")
    private String review;

    public static VerificationDto fromEntity(Verification verification) {
        Place place = verification.getPlace();
        return VerificationDto.builder()
                .vuid(verification.getVuid())
                .userId(verification.getUser().getUuid())
                .placeId(place != null ? place.getPk().getPuid() : null)
                .placeAddress(place != null ? place.getPk().getAddress() : null)
                .star(verification.getStar())
                .review(verification.getReview())
                .build();
    }

    public Verification toEntity(User user, Place place) {
        Verification verification = new Verification();
        // 필드 설정 로직 추가 필요
        return verification;
    }
}