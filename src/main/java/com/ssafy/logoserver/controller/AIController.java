package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.ai.dto.ChatRequest;
import com.ssafy.logoserver.domain.ai.dto.ChatResponse;
import com.ssafy.logoserver.domain.ai.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "AI Chat API", description = "AI 챗봇 서비스 API")
public class AIController {

    private final AIService AIService;

    public AIController(AIService AIService) {
        this.AIService = AIService;
    }

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
}