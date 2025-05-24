package com.ssafy.logoserver.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 좋아요 생성 요청 DTO
 * 장소에 대한 좋아요 추가 시 사용되는 데이터 전송 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 좋아요 생성 요청 DTO")
public class UserLikeRequestDto {

    @NotBlank(message = "주소는 필수 입력값입니다.")
    @Schema(description = "장소 주소", example = "서울특별시 강남구 테헤란로 212", required = true)
    private String address;

    @NotNull(message = "시/도 코드는 필수 입력값입니다.")
    @Schema(description = "시/도 코드", example = "11", required = true)
    private Long region;

    @NotNull(message = "시/군/구 코드는 필수 입력값입니다.")
    @Schema(description = "시/군/구 코드", example = "11680", required = true)
    private Long sig;

    @NotBlank(message = "장소 이름은 필수 입력값입니다.")
    @Schema(description = "장소 이름", example = "강남역", required = true)
    private String name;

    @Schema(description = "위도", example = "37.5012")
    private Double latitude;

    @Schema(description = "경도", example = "127.0396")
    private Double longitude;
}