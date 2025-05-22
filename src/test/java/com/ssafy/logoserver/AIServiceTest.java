package com.ssafy.logoserver;

import com.ssafy.logoserver.domain.ai.service.AIService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class AIServiceTest {

//    @MockBean
    private ChatClient chatClient;

    @Autowired
    private AIService AIService;

    @Test
    void testChatWithoutChatId() {
        // 테스트 구현
    }

    @Test
    void testChatWithChatId() {
        // 테스트 구현
    }
}
