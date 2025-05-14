package com.ssafy.logoserver.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "채팅 응답 DTO")
public class ChatResponse {

    @Schema(description = "채팅 세션 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID chatId;

    @Schema(description = "AI 응답 내용", example = "오늘은 맑고 화창한 날씨입니다.")
    private String answer;
}