package com.ssafy.logoserver.service;

import com.ssafy.logoserver.domain.ai.dto.ChatRequest;
import com.ssafy.logoserver.domain.ai.dto.ChatResponse;
import com.ssafy.logoserver.domain.ai.service.AIService;
import com.ssafy.logoserver.domain.notion.dto.NotionIntegrationDto;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import com.ssafy.logoserver.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Notion AI 통합 서비스
 * AI 분석과 Notion 페이지 작성을 연결하는 통합 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotionIntegrationService {

    private final AIService aiService;
    private final NotionService notionService;
    private final UserRepository userRepository;

    /**
     * AI 분석 후 Notion 페이지에 결과 작성
     * @param request Notion 통합 요청 DTO
     * @return 처리 결과
     */
    @Transactional
    public NotionIntegrationDto.Response processAiToNotion(NotionIntegrationDto request) {
        try {
            log.info("AI-Notion 통합 처리 시작");

            // 1. 현재 사용자 확인 (선택사항 - 로그인 없이도 사용 가능하도록)
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId != null) {
                log.info("인증된 사용자 요청 - 사용자 ID: {}", currentUserId);
            } else {
                log.info("비인증 요청 - 임시 처리");
            }

            // 2. AI 서비스를 통해 질문 분석
            ChatRequest chatRequest = new ChatRequest();
            chatRequest.setChatId(UUID.randomUUID()); // 새로운 채팅 세션
            chatRequest.setQuestion(request.getQuestion());

            log.info("AI 분석 요청 - 질문 길이: {} 글자", request.getQuestion().length());

            ChatResponse aiResponse = aiService.chatWithAnthropic(chatRequest);

            if (aiResponse == null || aiResponse.getAnswer() == null || aiResponse.getAnswer().trim().isEmpty()) {
                log.error("AI 응답이 비어있음");
                return NotionIntegrationDto.Response.builder()
                        .success(false)
                        .notionWriteSuccess(false)
                        .message("AI 분석 실패")
                        .errorMessage("AI로부터 응답을 받지 못했습니다.")
                        .build();
            }

            log.info("AI 분석 완료 - 응답 길이: {} 글자", aiResponse.getAnswer().length());

            // 3. Notion 페이지 존재 확인
            boolean pageExists = notionService.checkPageExists(
                    request.getNotionAccessToken(),
                    request.getNotionPageId()
            );

            if (!pageExists) {
                log.error("Notion 페이지가 존재하지 않거나 접근 권한이 없음 - pageId: {}",
                        request.getNotionPageId());
                return NotionIntegrationDto.Response.builder()
                        .success(true) // AI 분석은 성공
                        .aiResponse(aiResponse.getAnswer())
                        .notionWriteSuccess(false)
                        .message("AI 분석은 완료되었으나 Notion 페이지 작성 실패")
                        .errorMessage("Notion 페이지에 접근할 수 없습니다. 페이지 ID와 액세스 토큰을 확인해주세요.")
                        .build();
            }

            // 4. Notion 페이지에 AI 응답 작성
            boolean notionWriteSuccess = notionService.writeToNotionPage(
                    request.getNotionAccessToken(),
                    request.getNotionPageId(),
                    aiResponse.getAnswer()
            );

            // 5. 사용자의 notionPageId 업데이트 (로그인한 사용자인 경우)
            if (currentUserId != null && notionWriteSuccess) {
                try {
                    updateUserNotionPageId(currentUserId, request.getNotionPageId());
                } catch (Exception e) {
                    log.warn("사용자 Notion 페이지 ID 업데이트 실패", e);
                    // 이 오류는 전체 처리에 영향을 주지 않음
                }
            }

            // 6. 결과 반환
            if (notionWriteSuccess) {
                log.info("AI-Notion 통합 처리 완료 - 성공");
                return NotionIntegrationDto.Response.builder()
                        .success(true)
                        .aiResponse(aiResponse.getAnswer())
                        .notionWriteSuccess(true)
                        .message("AI 분석 완료 및 Notion 페이지 작성 성공")
                        .build();
            } else {
                log.error("Notion 페이지 작성 실패");
                return NotionIntegrationDto.Response.builder()
                        .success(true) // AI 분석은 성공
                        .aiResponse(aiResponse.getAnswer())
                        .notionWriteSuccess(false)
                        .message("AI 분석은 완료되었으나 Notion 페이지 작성 실패")
                        .errorMessage("Notion 페이지 작성 중 오류가 발생했습니다.")
                        .build();
            }

        } catch (Exception e) {
            log.error("AI-Notion 통합 처리 중 오류 발생", e);
            return NotionIntegrationDto.Response.builder()
                    .success(false)
                    .notionWriteSuccess(false)
                    .message("처리 중 오류 발생")
                    .errorMessage("서버 내부 오류: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 사용자의 Notion 페이지 ID 업데이트
     * @param userId 사용자 ID
     * @param notionPageId Notion 페이지 ID
     */
    private void updateUserNotionPageId(String userId, String notionPageId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

            // 기존 notionPageId와 다른 경우에만 업데이트
            if (!notionPageId.equals(user.getNotionPageId())) {
                User updatedUser = User.builder()
                        .uuid(user.getUuid())
                        .id(user.getId())
                        .password(user.getPassword())
                        .name(user.getName())
                        .email(user.getEmail())
                        .gender(user.getGender())
                        .nickname(user.getNickname())
                        .birthday(user.getBirthday())
                        .profileImage(user.getProfileImage())
                        .provider(user.getProvider())
                        .providerId(user.getProviderId())
                        .role(user.getRole())
                        .notionPageId(notionPageId) // 새로운 Notion 페이지 ID 설정
                        .created(user.getCreated())
                        .build();

                userRepository.save(updatedUser);

                log.info("사용자 Notion 페이지 ID 업데이트 완료 - 사용자: {}, 페이지 ID: {}",
                        userId, notionPageId);
            }
        } catch (Exception e) {
            log.error("사용자 Notion 페이지 ID 업데이트 실패 - 사용자: {}", userId, e);
            throw e;
        }
    }

    /**
     * 여행 데이터를 AI 분석용 질문으로 포맷팅 (유틸리티 메서드)
     * @param travelData 여행 데이터 JSON 문자열
     * @return 포맷팅된 AI 질문
     */
    public String formatTravelDataForAi(String travelData) {
        return String.format(
                "다음 여행 일정을 상세히 분석하고 여행 가이드를 작성해주세요. " +
                        "각 장소의 특징, 이동 시간, 추천 활동, 주의사항 등을 포함하여 " +
                        "실용적이고 유용한 여행 가이드로 만들어주세요.\n\n" +
                        "여행 정보: %s\n\n" +
                        "다음 항목들을 포함하여 분석해주세요:\n" +
                        "1. 전체 일정 개요\n" +
                        "2. 일자별 상세 분석\n" +
                        "3. 각 장소별 특징 및 팁\n" +
                        "4. 예상 소요 시간 및 이동 경로\n" +
                        "5. 추천 활동 및 주의사항\n" +
                        "6. 예산 활용 팁",
                travelData
        );
    }

    /**
     * Notion 페이지 접근 가능 여부 확인 (컨트롤러에서 직접 사용)
     * @param accessToken Notion 액세스 토큰
     * @param pageId 확인할 페이지 ID
     * @return 페이지 접근 가능 여부
     */
    public boolean checkPageExists(String accessToken, String pageId) {
        return notionService.checkPageExists(accessToken, pageId);
    }
}