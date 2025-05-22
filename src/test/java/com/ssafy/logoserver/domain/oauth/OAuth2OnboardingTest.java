// src/test/java/com/ssafy/logoserver/domain/oauth/OAuth2OnboardingTest.java
package com.ssafy.logoserver.domain.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.logoserver.domain.user.dto.OAuth2UserCompletionDto;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OAuth2OnboardingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testOAuth2User;

    @BeforeEach
    void setUp() {
        // 테스트 전에 기존 데이터 정리
        userRepository.deleteAll();

        // OAuth2 신규 사용자 생성 (성별, 생년월일이 없는 상태)
        testOAuth2User = User.builder()
                .id("google_123456789")
                .name("구글사용자")
                .email("googleuser@gmail.com")
                .nickname("구글사용자")
                .provider("google")
                .providerId("123456789")
                .profileImage("https://example.com/profile.jpg")
                .role(User.Role.USER)
                // gender와 birthday는 null로 설정하여 추가 정보가 필요한 상태
                .build();

        testOAuth2User = userRepository.save(testOAuth2User);
    }

    @Test
    @DisplayName("OAuth2 사용자 추가 정보 완성 성공")
    void completeOAuth2UserInfoSuccess() throws Exception {
        OAuth2UserCompletionDto completionDto = OAuth2UserCompletionDto.builder()
                .userId(testOAuth2User.getUuid())
                .gender("M")
                .birthday(LocalDate.of(1990, 5, 15))
                .nickname("새로운닉네임")
                .notionPageId("12345abc-6789-def0-1234-56789abcdef0")
                .build();

        mockMvc.perform(post("/api/oauth2/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completionDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.gender", is("M")))
                .andExpect(jsonPath("$.data.nickname", is("새로운닉네임")))
                .andExpect(jsonPath("$.data.notionPageId", is("12345abc-6789-def0-1234-56789abcdef0")));
    }

    @Test
    @DisplayName("OAuth2 사용자 추가 정보 완성 - 필수 필드 누락 시 실패")
    void completeOAuth2UserInfoFailMissingRequired() throws Exception {
        OAuth2UserCompletionDto completionDto = OAuth2UserCompletionDto.builder()
                .userId(testOAuth2User.getUuid())
                // gender와 birthday 누락
                .nickname("새로운닉네임")
                .build();

        mockMvc.perform(post("/api/oauth2/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completionDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("OAuth2 사용자 추가 정보 완성 - 중복 닉네임 시 실패")
    void completeOAuth2UserInfoFailDuplicateNickname() throws Exception {
        // 중복 닉네임을 가진 다른 사용자 생성
        User anotherUser = User.builder()
                .id("another_user")
                .name("다른사용자")
                .email("another@example.com")
                .gender("F")
                .nickname("중복닉네임")
                .birthday(LocalDate.of(1995, 1, 1))
                .role(User.Role.USER)
                .build();
        userRepository.save(anotherUser);

        OAuth2UserCompletionDto completionDto = OAuth2UserCompletionDto.builder()
                .userId(testOAuth2User.getUuid())
                .gender("M")
                .birthday(LocalDate.of(1990, 5, 15))
                .nickname("중복닉네임") // 중복된 닉네임
                .build();

        mockMvc.perform(post("/api/oauth2/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completionDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다: 중복닉네임"));
    }

    @Test
    @DisplayName("온보딩 상태 확인 - 쿠키 없음")
    void getOnboardingStatusNoCookies() throws Exception {
        mockMvc.perform(get("/api/oauth2/onboarding/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.needsAdditionalInfo", is(false)))
                .andExpect(jsonPath("$.data.isNewUser", is(false)));
    }

    @Test
    @DisplayName("온보딩 상태 확인 - 추가 정보 필요 쿠키 있음")
    void getOnboardingStatusWithCookies() throws Exception {
        mockMvc.perform(get("/api/oauth2/onboarding/status")
                        .cookie(new jakarta.servlet.http.Cookie("needs_additional_info", "true"))
                        .cookie(new jakarta.servlet.http.Cookie("is_new_user", "true"))
                        .cookie(new jakarta.servlet.http.Cookie("user_id", testOAuth2User.getUuid().toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.needsAdditionalInfo", is(true)))
                .andExpect(jsonPath("$.data.isNewUser", is(true)))
                .andExpect(jsonPath("$.data.userId", is(testOAuth2User.getUuid().intValue())));
    }
}