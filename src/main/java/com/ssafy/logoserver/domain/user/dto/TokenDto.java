package com.ssafy.logoserver.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "토큰 정보 DTO")
public class TokenDto {

    @Schema(description = "토큰 인증 타입" , example = "Bearer", required = true)
    private String grantType;

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzUxMiJ9...", required = true)
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzUxMiJ9...", required = true)
    private String refreshToken;
}