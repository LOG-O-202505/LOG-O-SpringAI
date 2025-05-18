package com.ssafy.logoserver.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 갱신 요청 DTO")
public class TokenRefreshRequestDto {

    @NotBlank(message = "리프레시 토큰은 필수 입력값입니다.")
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzUxMiJ9...", required = true)
    private String refreshToken;
}