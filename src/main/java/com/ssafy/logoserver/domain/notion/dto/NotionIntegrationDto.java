package com.ssafy.logoserver.domain.notion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Notion 통합 요청 DTO
 * AI 분석을 통해 Notion에 내용을 작성하기 위한 요청 데이터
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Notion AI 통합 요청 DTO")
public class NotionIntegrationDto {

    @Schema(description = "사용자 질문/요청 내용",
            example = "제주도 3일 여행 일정을 분석해주세요. 여행 정보: {...}",
            required = true)
    private String question;

    @Schema(description = "Notion 액세스 토큰",
            example = "ntn_abcd1234...",
            required = true)
    private String notionAccessToken;

    @Schema(description = "작성할 Notion 페이지 ID",
            example = "b55c9c91-384d-452b-81db-d1ef79372b75",
            required = true)
    private String notionPageId;

    /**
     * Notion AI 통합 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Notion AI 통합 응답 DTO")
    public static class Response {

        @Schema(description = "처리 성공 여부", example = "true")
        private boolean success;

        @Schema(description = "AI 생성 내용", example = "제주도 3일 여행 분석 결과...")
        private String aiResponse;

        @Schema(description = "Notion 작성 성공 여부", example = "true")
        private boolean notionWriteSuccess;

        @Schema(description = "처리 메시지", example = "AI 분석 완료 및 Notion 페이지 작성 성공")
        private String message;

        @Schema(description = "오류 메시지 (실패 시)")
        private String errorMessage;
    }
}