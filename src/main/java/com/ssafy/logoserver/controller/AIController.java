package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.ai.dto.ChatRequest;
import com.ssafy.logoserver.domain.ai.dto.ChatResponse;
import com.ssafy.logoserver.domain.ai.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class AIController {

    private final AIService AIService;

    public AIController(AIService AIService) {
        this.AIService = AIService;
    }

    @PostMapping("/openai")
    public ResponseEntity<ChatResponse> chatWithOpenAi(@RequestBody ChatRequest chatRequest) {
        ChatResponse chatResponse = AIService.chatWithOpenAi(chatRequest);
        return ResponseEntity.ok(chatResponse);
    }

    @PostMapping("/anthropic")
    public ResponseEntity<ChatResponse> chatWithAnthropic(@RequestBody ChatRequest chatRequest) {
        ChatResponse chatResponse = AIService.chatWithAnthropic(chatRequest);
        return ResponseEntity.ok(chatResponse);
    }
}