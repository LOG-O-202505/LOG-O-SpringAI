package com.ssafy.logoserver.domain.ai.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRequest {
    private UUID chatId;
    private String question;
}