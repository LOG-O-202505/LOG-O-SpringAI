package com.ssafy.logoserver.domain.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "방문 인증 요청 DTO")
public class VerificationRequestDto {

    @Schema(description = "여행 장소 고유 ID", example = "1")
    private Long pid;

    @Schema(description = "여행 장소 주소", example = "서울특별시 강남구 테헤란로 212")
    private String address;

    @Schema(description = "여행 장소 후기", example = "좋은 곳이었습니다.")
    private String review;

    @Schema(description = "여행 장소 평점", example = "4.5")
    private Double star;

    @Schema(description = "인코딩된 이미지 데이터", example = "base64 encoded string")
    private String image;
}