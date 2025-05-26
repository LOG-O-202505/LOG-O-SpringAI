package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.travel.entity.Verification;
import com.ssafy.logoserver.domain.user.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 위치 인증 상세 정보 DTO
 * 인증 정보와 함께 작성자 정보, 이미지 URL을 포함하여 전달하는 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "위치 인증 상세 DTO")
public class VerificationDetailDto {

    /**
     * 인증 고유 ID
     */
    @Schema(description = "인증 ID", example = "1")
    private Long vuid;

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
     */
    @Schema(description = "인증 생성 시간", example = "2025-05-26T10:30:00")
    private LocalDateTime created;

    /**
     * 인증을 작성한 사용자의 상세 정보
     */
    @Schema(description = "작성자 정보")
    private UserDto user;

    /**
     * 인증 이미지 URL (MinIO Presigned URL)
     * 이미지가 없는 경우 null
     */
    @Schema(description = "인증 이미지 URL", example = "https://minio.example.com/bucket/verification-image.jpg")
    private String imageUrl;

    /**
     * Verification 엔티티를 상세 DTO로 변환하는 정적 메서드
     *
     * @param verification 변환할 Verification 엔티티
     * @param user 작성자 정보 DTO
     * @param imageUrl MinIO에서 생성된 이미지 Presigned URL (없으면 null)
     * @return 변환된 VerificationDetailDto 객체
     */
    public static VerificationDetailDto fromEntity(Verification verification, UserDto user, String imageUrl) {
        return VerificationDetailDto.builder()
                .vuid(verification.getVuid())
                .placeId(verification.getPlace() != null ? verification.getPlace().getPuid() : null)
                .placeAddress(verification.getPlace() != null ? verification.getPlace().getAddress() : null)
                .star(verification.getStar())
                .review(verification.getReview())
                .created(verification.getCreated())
                .user(user) // 작성자의 모든 정보 포함
                .imageUrl(imageUrl) // MinIO 이미지 URL 포함
                .build();
    }
}