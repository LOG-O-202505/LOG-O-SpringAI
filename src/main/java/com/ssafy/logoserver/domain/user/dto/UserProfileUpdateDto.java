package com.ssafy.logoserver.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 프로필 업데이트 DTO")
public class UserProfileUpdateDto {

    @Schema(description = "수정할 사용자 닉네임", example = "새로운닉네임")
    private String nickname;

    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate birthday;

    @Schema(description = "노션 페이지 ID", example = "12345abc-6789-def0-1234-56789abcdef0")
    private String notionPageId;
}