package com.ssafy.logoserver.domain.area.dto;

import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.travel.dto.VerificationDetailDto;
import com.ssafy.logoserver.domain.user.dto.UserLikeDetailDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 장소 상세 정보 DTO
 * 장소 기본 정보와 해당 장소의 모든 인증 정보, 현재 사용자의 좋아요 정보를 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "장소 상세 정보 DTO")
public class PlaceDetailDto {

    /**
     * 장소 고유 ID
     */
    @Schema(description = "장소 ID", example = "1")
    private Long puid;

    /**
     * 장소 주소
     */
    @Schema(description = "장소 주소", example = "제주특별자치도 서귀포시 성산읍 일출로 284-12")
    private String address;

    /**
     * 지역 ID
     */
    @Schema(description = "지역 ID", example = "1")
    private Long areaId;

    /**
     * 장소 이름
     */
    @Schema(description = "장소 이름", example = "성산일출봉")
    private String name;

    /**
     * 위도
     */
    @Schema(description = "위도", example = "33.459198")
    private Double latitude;

    /**
     * 경도
     */
    @Schema(description = "경도", example = "126.942394")
    private Double longitude;

    /**
     * 해당 장소의 모든 인증 정보 목록 (최신순 정렬)
     * 각 인증 정보에는 작성자 정보와 이미지 URL이 포함됨
     */
    @Schema(description = "장소 인증 목록 (최신순)")
    private List<VerificationDetailDto> verifications;

    /**
     * 현재 로그인한 사용자가 이 장소를 좋아요했는지 여부
     */
    @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
    private boolean isLikedByCurrentUser;

    /**
     * 현재 사용자의 좋아요 정보 (좋아요한 경우에만 포함)
     * 좋아요하지 않은 경우 null
     */
    @Schema(description = "현재 사용자의 좋아요 정보")
    private UserLikeDetailDto currentUserLike;

    /**
     * 해당 장소의 총 좋아요 수
     */
    @Schema(description = "총 좋아요 수", example = "15")
    private int totalLikeCount;

    /**
     * 해당 장소의 평균 별점
     * 인증된 리뷰들의 별점 평균값
     */
    @Schema(description = "평균 별점", example = "4.2")
    private Double averageRating;

    /**
     * 해당 장소의 총 리뷰 수
     */
    @Schema(description = "총 리뷰 수", example = "8")
    private int totalReviewCount;

    /**
     * Place 엔티티와 관련 정보들을 상세 DTO로 변환하는 정적 메서드
     *
     * @param place 장소 엔티티
     * @param verifications 해당 장소의 인증 목록 (최신순 정렬됨)
     * @param isLikedByCurrentUser 현재 사용자의 좋아요 여부
     * @param currentUserLike 현재 사용자의 좋아요 정보 (없으면 null)
     * @param totalLikeCount 총 좋아요 수
     * @param averageRating 평균 별점
     * @param totalReviewCount 총 리뷰 수
     * @return 변환된 PlaceDetailDto 객체
     */
    public static PlaceDetailDto fromEntity(Place place,
                                            List<VerificationDetailDto> verifications,
                                            boolean isLikedByCurrentUser,
                                            UserLikeDetailDto currentUserLike,
                                            int totalLikeCount,
                                            Double averageRating,
                                            int totalReviewCount) {
        return PlaceDetailDto.builder()
                .puid(place.getPuid())
                .address(place.getAddress())
                .areaId(place.getArea() != null ? place.getArea().getAuid() : null)
                .name(place.getName())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .verifications(verifications)
                .isLikedByCurrentUser(isLikedByCurrentUser)
                .currentUserLike(currentUserLike)
                .totalLikeCount(totalLikeCount)
                .averageRating(averageRating)
                .totalReviewCount(totalReviewCount)
                .build();
    }
}