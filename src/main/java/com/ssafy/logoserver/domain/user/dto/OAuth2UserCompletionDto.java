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
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "OAuth2 사용자 추가 정보 완성 DTO")
public class OAuth2UserCompletionDto {

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

    /**
     * 노션 페이지 ID (선택사항)
     * 사용자의 노션 페이지 연동을 위한 ID
     */
    @Schema(description = "노션 페이지 ID", example = "12345abc-6789-def0-1234-56789abcdef0")
    private String notionPageId;

    /**
     * 닉네임 변경 (선택사항)
     * 입력하지 않으면 기존 닉네임 유지
     */
    @NotNull(message = "닉네임은 필수입니다.")
    @Schema(description = "닉네임 ", example = "새로운닉네임")
    private String nickname;
}