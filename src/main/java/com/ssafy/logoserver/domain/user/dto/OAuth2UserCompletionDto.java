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

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "OAuth2 사용자 추가 정보 완성 DTO")
public class OAuth2UserCompletionDto {

    @NotNull(message = "사용자 ID는 필수입니다.")
    @Schema(description = "사용자 UUID", example = "1", required = true)
    private Long userId;

    @NotBlank(message = "성별은 필수입니다.")
    @Schema(description = "성별", example = "M", required = true)
    private String gender;

    @NotNull(message = "생년월일은 필수입니다.")
    @Schema(description = "생년월일", example = "1990-01-01", required = true)
    private LocalDate birthday;

    @Schema(description = "노션 페이지 ID", example = "12345abc-6789-def0-1234-56789abcdef0")
    private String notionPageId;

    @Schema(description = "닉네임 변경 (선택사항)", example = "새로운닉네임")
    private String nickname;
}