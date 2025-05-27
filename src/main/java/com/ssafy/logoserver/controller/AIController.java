package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.ai.dto.ChatRequest;
import com.ssafy.logoserver.domain.ai.dto.ChatResponse;
import com.ssafy.logoserver.domain.ai.service.AIService;
import com.ssafy.logoserver.service.NotionService;
import com.ssafy.logoserver.utils.ResponseUtil;
import com.ssafy.logoserver.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "AI Chat API", description = "AI 챗봇 서비스 API")
@Slf4j
public class AIController {

    private final AIService AIService;
    private final NotionService notionService;

    @PostMapping("/openai")
    @Operation(summary = "OpenAI 모델과 채팅", description = "OpenAI 모델을 사용한 채팅 응답을 제공합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<ChatResponse> chatWithOpenAi(
            @Parameter(description = "채팅 요청 정보", required = true)
            @RequestBody ChatRequest chatRequest) {
        ChatResponse chatResponse = AIService.chatWithOpenAi(chatRequest);
        return ResponseEntity.ok(chatResponse);
    }

    @PostMapping("/anthropic")
    @Operation(summary = "Anthropic Claude 모델과 채팅", description = "Anthropic Claude 모델을 사용한 채팅 응답을 제공합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<ChatResponse> chatWithAnthropic(
            @Parameter(description = "채팅 요청 정보", required = true)
            @RequestBody ChatRequest chatRequest) {
        ChatResponse chatResponse = AIService.chatWithAnthropic(chatRequest);
        return ResponseEntity.ok(chatResponse);
    }

    // 새로운 마크다운 파일 다운로드 메서드
    @GetMapping("/anthropic/markdown/{tuid}")
    @Operation(summary = "Anthropic Claude 모델과 채팅 (마크다운 파일 다운로드)",
            description = "Anthropic Claude 모델을 사용한 채팅 응답을 .md 파일로 다운로드합니다.")
    public ResponseEntity<Resource> chatWithAnthropicAsMarkdown(
            @PathVariable("tuid") Long tuid) throws IOException {

        String currentUserId = SecurityUtil.getCurrentUserId();

        // 질문 만들기
        log.info("여행 데이터 기반 AI 질문 생성 시작");
        String question = notionService.getQuestionByTravelId(tuid, currentUserId);

        if (question == null || question.trim().isEmpty()) {
            log.error("AI 질문 생성 실패 - tuid: {}", tuid);
        }

        ChatRequest chatRequest = ChatRequest.builder()
                .question(question)
                .build();
        ChatResponse chatResponse = AIService.chatWithAnthropic(chatRequest);

        // 마크다운 콘텐츠를 바이트 배열로 변환
        byte[] markdownContent = chatResponse.getAnswer().getBytes(StandardCharsets.UTF_8);

        // ByteArrayResource 생성
        ByteArrayResource resource = new ByteArrayResource(markdownContent);

        // 파일명 생성 (날짜 + chatId 사용)
        String filename = String.format("travel-guide-%s-%s.md",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")),
                chatResponse.getChatId().toString().substring(0, 8));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/markdown; charset=UTF-8")
                .contentLength(markdownContent.length)
                .body(resource);
    }
}