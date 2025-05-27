package com.ssafy.logoserver.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class AIConfig {

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public ChatClient openAiChatClient(
            @Qualifier("openAiChatModel") ChatModel chatModel,
            ChatMemory chatMemory,
            @Value("classpath:prompts/client-to-claude-prompt.st") Resource systemPrompt
    ) {
        return ChatClient
                .builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    @Bean
    public ChatClient anthropicChatClient(
            @Qualifier("anthropicChatModel") ChatModel chatModel,
            ChatMemory chatMemory,
            @Value("classpath:prompts/client-to-claude-prompt.st") Resource systemPrompt
    ) {
        return ChatClient
                .builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .defaultOptions(ChatOptions.builder()
                        .maxTokens(40000)
                        .temperature(0.7)
                        .build())
                .build();
    }
}