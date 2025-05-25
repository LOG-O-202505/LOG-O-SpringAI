// src/main/java/com/ssafy/logoserver/domain/user/dto/OAuth2UserCompletionDto.java
package com.ssafy.logoserver.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * OAuth2 사용자 추가 정보 완성 DTO
 * OAuth2 로그인 후 필수 정보 입력을 위한 데이터 전송 객체
 * 사용자 ID는 보안 컨텍스트에서 자동으로 추출하므로 포함하지 않음
 *
 * 필수 입력 필드: nickname, gender, birthday (3개 필드 모두 필수)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "OAuth2 사용자 추가 정보 완성 DTO")
public class OAuth2UserCompletionDto {

    /**
     * 닉네임 (필수)
     * OAuth2 로그인 시 사용자가 직접 설정하는 닉네임
     * 2-15자 제한, 중복 불가
     */
    @NotBlank(message = "닉네임은 필수입니다.")
    @Schema(description = "닉네임", example = "멋진여행자", required = true)
    private String nickname;

    /**
     * 사용자 성별 (필수)
     * M: 남성, F: 여성, O: 기타
     */
    @NotBlank(message = "성별은 필수입니다.")
    @Schema(description = "성별", example = "M", required = true,
            allowableValues = {"M", "F", "O"})
    private String gender;

    /**
     * 사용자 생년월일 (필수)
     * ISO 8601 형식의 날짜 (YYYY-MM-DD)
     */
    @NotNull(message = "생년월일은 필수입니다.")
    @Schema(description = "생년월일", example = "1990-01-01", required = true)
    private LocalDate birthday;
}