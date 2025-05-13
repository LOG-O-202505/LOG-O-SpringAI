package com.ssafy.logoserver.domain.ai.service;

import com.ssafy.logoserver.domain.ai.dto.ChatRequest;
import com.ssafy.logoserver.domain.ai.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AIService {

    private final ChatClient openAiChatClient;
    private final ChatClient anthropicChatClient;

    public AIService(
            @Qualifier("openAiChatClient") ChatClient openAiChatClient,
            @Qualifier("anthropicChatClient") ChatClient anthropicChatClient) {
        this.openAiChatClient = openAiChatClient;
        this.anthropicChatClient = anthropicChatClient;
    }

    public ChatResponse chatWithOpenAi(ChatRequest chatRequest) {
        UUID chatId = Optional
                .ofNullable(chatRequest.getChatId())
                .orElse(UUID.randomUUID());

        String answer = openAiChatClient
                .prompt()
                .user(chatRequest.getQuestion())
                .advisors(advisorSpec ->
                        advisorSpec
                                .param("chat_memory_conversation_id", chatId))
                .call()
                .content();

        return new ChatResponse(chatId, answer);
    }

    public ChatResponse chatWithAnthropic(ChatRequest chatRequest) {
        UUID chatId = Optional
                .ofNullable(chatRequest.getChatId())
                .orElse(UUID.randomUUID());

        String answer = anthropicChatClient
                .prompt()
                .user(chatRequest.getQuestion())
                .advisors(advisorSpec ->
                        advisorSpec
                                .param("chat_memory_conversation_id", chatId))
                .call()
                .content();

        return new ChatResponse(chatId, answer);
    }
}