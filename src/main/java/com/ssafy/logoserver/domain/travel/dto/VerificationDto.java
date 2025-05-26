package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.travel.entity.Verification;
import com.ssafy.logoserver.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 위치 인증 정보를 전송하기 위한 DTO
 * 클라이언트와 서버 간 위치 인증 데이터 교환에 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "위치 인증 DTO")
public class VerificationDto {

    /**
     * 인증 고유 ID
     */
    @Schema(description = "인증 ID", example = "1")
    private Long vuid;

    /**
     * 인증을 수행한 사용자 ID
     */
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    /**
     * 인증한 장소 ID
     */
    @Schema(description = "장소 ID", example = "1")
    private Long placeId;

    /**
     * 인증한 장소 주소 정보
     */
    @Schema(description = "장소 주소", example = "제주특별자치도 서귀포시 성산읍 일출로 284-12")
    private String placeAddress;

    /**
     * 사용자가 매긴 별점 (1.0 ~ 5.0)
     */
    @Schema(description = "별점", example = "4.5")
    private Double star;

    /**
     * 사용자 리뷰 내용
     */
    @Schema(description = "리뷰", example = "너무 멋진 곳이었어요!")
    private String review;

    /**
     * 인증 생성 시간
     * 사용자가 언제 이 장소를 인증했는지를 나타내는 타임스탬프
     */
    @Schema(description = "인증 생성 시간", example = "2025-05-26T10:30:00")
    private LocalDateTime created;

    /**
     * Verification 엔티티를 DTO로 변환하는 정적 메서드
     *
     * @param verification 변환할 Verification 엔티티
     * @return 변환된 VerificationDto 객체
     */
    public static VerificationDto fromEntity(Verification verification) {
        Place place = verification.getPlace();
        return VerificationDto.builder()
                .vuid(verification.getVuid())
                .userId(verification.getUser().getUuid())
                .placeId(place != null ? place.getPuid() : null)
                .placeAddress(place != null ? place.getAddress() : null)
                .star(verification.getStar())
                .review(verification.getReview())
                .created(verification.getCreated()) // 생성 시간 정보 포함
                .build();
    }

    /**
     * DTO를 Verification 엔티티로 변환하는 메서드
     * 주로 새로운 인증 생성 시 사용됨
     * created 필드는 @CreationTimestamp에 의해 자동 생성되므로 설정하지 않음
     *
     * @param user 인증을 수행하는 사용자 엔티티
     * @param place 인증할 장소 엔티티
     * @return 생성된 Verification 엔티티
     */
    public Verification toEntity(User user, Place place) {
        return Verification.builder()
                .user(user)
                .place(place)
                .star(star != null ? star : 0.0)
                .review(review)
                // created 필드는 @CreationTimestamp에 의해 자동 설정되므로 별도로 설정하지 않음
                .build();
    }
}