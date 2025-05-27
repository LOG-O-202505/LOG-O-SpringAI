package com.ssafy.logoserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.logoserver.domain.notion.dto.NotionIntegrationDto;
import com.ssafy.logoserver.domain.travel.dto.TravelIdDto;
import com.ssafy.logoserver.domain.user.service.UserService;
import com.ssafy.logoserver.service.NotionIntegrationService;
import com.ssafy.logoserver.service.NotionService;
import com.ssafy.logoserver.service.NotionTokenRequester;
import com.ssafy.logoserver.utils.ResponseUtil;
import com.ssafy.logoserver.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Notion AI 통합 컨트롤러
 * AI 분석 후 Notion 페이지에 결과를 작성하는 통합 API를 제공
 */
@Controller
@RequestMapping("/api/notion")
@Tag(name = "Notion Integration API", description = "AI 분석 결과를 Notion에 작성하는 통합 API")
@Slf4j
public class NotionIntegrationController {

    @Value("${notion.uri.redirect-url}")
    private String callBackUrl; // redirection url
    @Value("${notion.client.id}")
    private String clientId; //clientId
    @Value("${notion.uri.authorize-base-url}")
    private String authorizeUrl;
    @Value("${notion.client.secret}")
    private String clientPw; //clientPw

    private final UserService userService;
    // Travel ID를 임시 저장하기 위한 메모리 저장소 (실제 환경에서는 Redis 사용 권장)
    private final Map<String, Long> sessionTravelMap = new ConcurrentHashMap<>();
    // User ID를 임시 저장하기 위한 메모리 저장소
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    private final NotionIntegrationService notionIntegrationService;
    private final NotionService notionService;
    public NotionIntegrationController(WebClient.Builder webClientBuilder,
                                       NotionIntegrationService notionIntegrationService,
                                       NotionService notionService,
                                       TravelIdDto travelIdDto,
                                       UserService userService) {
        this.notionIntegrationService = notionIntegrationService;
        this.notionService = notionService;
        this.userService = userService;
    }

    // 인증 url 을 생성해주는 메소드
    private String getAuthGrantType(String callbackURL) {
        return authorizeUrl + "&client_id=" + clientId + "&redirect_uri=" + callbackURL + "&response_type=code";
    }

    /**
     * 1. 노션 인증 시작 - 사용자를 노션 인증 페이지로 리다이렉트
     * @param tuid 여행 ID
     * @return 노션 인증 URL로 리다이렉트
     */
    @GetMapping("/{tuid}")
    @Operation(
            summary = "노션 인증 시작",
            description = "특정 여행(tuid)에 대한 노션 인증을 시작합니다. 사용자를 노션 인증 페이지로 리다이렉트합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "노션 인증 페이지로 리다이렉트"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    public String startNotionAuth(@PathVariable("tuid") Long tuid,
                                  @RequestParam(required = false) String userId) {
        try {
            log.info("노션 인증 시작 - tuid: {}", tuid);

            // 현재 사용자 확인
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                log.error("인증되지 않은 사용자의 요청");
                throw new RuntimeException("로그인이 필요합니다.");
            }

            // 세션에 tuid 저장 (실제로는 Redis나 DB 사용 권장)
            // 임시로 userId를 키로 사용하여 tuid 저장
            sessionTravelMap.put(userId, tuid);
            sessionUserMap.put("userId", userId);
            log.info("세션에 tuid 저장 완료 - 사용자: {}, tuid: {}", currentUserId, tuid);

            log.info("세션에 userId 저장 완료 - userId: {}", userId);

            // 노션 인증 URL 생성
            String authGrantType = getAuthGrantType(callBackUrl);
            log.info("노션 인증 URL 생성 완료: {}", authGrantType);

            return "redirect:" + authGrantType;

        } catch (Exception e) {
            log.error("노션 인증 시작 중 오류 발생 - tuid: {}", tuid, e);
            throw new RuntimeException("노션 인증 시작 실패: " + e.getMessage());
        }
    }

    /**
     * 2. 노션 인증 콜백 및 AI 분석 후 Notion 페이지 작성
     * 노션 인증 완료 후 자동으로 호출되며, AI 분석 후 결과를 노션에 작성합니다.
     *
     * @param code 노션에서 받은 인증 코드
     * @return AI 분석 결과와 Notion 작성 성공 여부
     */
    @GetMapping("/ai-to-notion")
    @ResponseBody
    @Operation(
            summary = "노션 인증 콜백 및 AI 분석 후 노션 작성",
            description = "노션 인증 완료 후 자동으로 호출되며, 여행 데이터를 AI로 분석한 후 결과를 노션 페이지에 작성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "처리 성공 (AI 분석 및 Notion 작성 결과 포함)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (인증 코드 누락 등)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> processAiToNotion(
            @Parameter(description = "노션 인증 코드", required = true)
            @RequestParam("code") String code) {
        try {
            log.info("노션 인증 콜백 시작 - Authorization Code: {}", code);

            // 1. 현재 사용자 확인
            String currentUserId = sessionUserMap.get("userId");

            // 2. 세션에서 tuid 가져오기
            Long tuid = sessionTravelMap.get(currentUserId);
            if (tuid == null) {
                log.error("세션에서 tuid를 찾을 수 없음 - 사용자: {}", currentUserId);
                return ResponseUtil.badRequest("여행 정보를 찾을 수 없습니다. 처음부터 다시 시도해주세요.");
            }

            log.info("세션에서 tuid 복원 완료 - 사용자: {}, tuid: {}", currentUserId, tuid);

            // 3. 노션 액세스 토큰 획득
            String notionAccessToken = handleRedirectUrl(code);
            if (notionAccessToken == null || notionAccessToken.trim().isEmpty()) {
                log.error("노션 액세스 토큰 획득 실패");
                return ResponseUtil.internalServerError("노션 인증에 실패했습니다.");
            }

            log.info("노션 액세스 토큰 획득 완료");

            // 질문 만들기
            log.info("여행 데이터 기반 AI 질문 생성 시작 - tuid: {}", tuid);
            String question = notionService.getQuestionByTravelId(tuid, currentUserId);

            if (question == null || question.trim().isEmpty()) {
                log.error("AI 질문 생성 실패 - tuid: {}", tuid);
                return ResponseUtil.badRequest("여행 데이터를 기반으로 질문을 생성할 수 없습니다.");
            }

            log.info("AI 질문 생성 완료 - 질문 길이: {} 글자", question.length());

            // 5. 사용자의 노션 페이지 ID 가져오기
            String notionPageId = userService.getNotionPageIdById(currentUserId);
            if (notionPageId == null || notionPageId.trim().isEmpty()) {
                log.error("사용자의 노션 페이지 ID가 설정되지 않음 - 사용자: {}", currentUserId);
                return ResponseUtil.badRequest("노션 페이지 ID가 설정되지 않았습니다. 프로필에서 설정해주세요.");
            }

            log.info("사용자 노션 페이지 ID 확인 완료: {}", notionPageId);

            // 6. NotionIntegrationDto 생성
            NotionIntegrationDto request = NotionIntegrationDto.builder()
                    .question(question)
                    .notionAccessToken(notionAccessToken)
                    .notionPageId(notionPageId)
                    .build();

            // 7. AI 분석 및 Notion 작성 처리
            log.info("AI 분석 및 노션 작성 시작");
            NotionIntegrationDto.Response response = notionIntegrationService.processAiToNotion(request);

            // 8. 세션 정리 (처리 완료 후)
            sessionTravelMap.remove(currentUserId);
            log.info("세션 정리 완료 - 사용자: {}", currentUserId);

            log.info("AI-Notion 통합 처리 완료 - 성공: {}, Notion 작성: {}",
                    response.isSuccess(), response.isNotionWriteSuccess());

            return ResponseUtil.success(response);

        } catch (Exception e) {
            log.error("AI-Notion 통합 처리 중 오류 발생", e);
            return ResponseUtil.internalServerError("처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 여행 데이터 전용 AI 분석 및 Notion 작성
     * 여행 데이터를 받아서 자동으로 AI 분석용 질문으로 포맷팅한 후 처리합니다.
     *
     * @param travelData        여행 데이터 JSON 문자열
     * @param notionAccessToken Notion 액세스 토큰
     * @param notionPageId      Notion 페이지 ID
     * @return AI 분석 결과와 Notion 작성 성공 여부
     */
    @PostMapping("/travel-to-notion")
    @ResponseBody
    @Operation(
            summary = "여행 데이터 AI 분석 후 Notion 작성",
            description = "여행 일정 데이터를 받아서 AI로 분석한 후, 그 결과를 Notion 페이지에 작성합니다. " +
                    "여행 데이터를 자동으로 AI 질문 형태로 변환하여 처리합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> processTravelToNotion(
            @Parameter(description = "여행 데이터 JSON 문자열", required = true)
            @RequestParam String travelData,
            @Parameter(description = "Notion 액세스 토큰", required = true)
            @RequestParam String notionAccessToken,
            @Parameter(description = "Notion 페이지 ID", required = true)
            @RequestParam String notionPageId) {

        try {
            log.info("여행 데이터 AI-Notion 통합 요청 수신 - 데이터 길이: {} 글자",
                    travelData != null ? travelData.length() : 0);

            // 필수 파라미터 검증
            if (travelData == null || travelData.trim().isEmpty()) {
                return ResponseUtil.badRequest("여행 데이터가 필요합니다.");
            }

            if (notionAccessToken == null || notionAccessToken.trim().isEmpty()) {
                return ResponseUtil.badRequest("Notion 액세스 토큰이 필요합니다.");
            }

            if (notionPageId == null || notionPageId.trim().isEmpty()) {
                return ResponseUtil.badRequest("Notion 페이지 ID가 필요합니다.");
            }

            // 여행 데이터를 AI 질문으로 포맷팅
            String formattedQuestion = notionIntegrationService.formatTravelDataForAi(travelData);

            // NotionIntegrationDto 생성
            NotionIntegrationDto request = NotionIntegrationDto.builder()
                    .question(formattedQuestion)
                    .notionAccessToken(notionAccessToken)
                    .notionPageId(notionPageId)
                    .build();

            // AI 분석 및 Notion 작성 처리
            NotionIntegrationDto.Response response = notionIntegrationService.processAiToNotion(request);

            log.info("여행 데이터 AI-Notion 통합 처리 완료 - 성공: {}, Notion 작성: {}",
                    response.isSuccess(), response.isNotionWriteSuccess());

            return ResponseUtil.success(response);

        } catch (Exception e) {
            log.error("여행 데이터 AI-Notion 통합 처리 중 오류 발생", e);
            return ResponseUtil.internalServerError("처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * Notion 페이지 접근 가능 여부 확인
     * 주어진 액세스 토큰으로 지정된 페이지에 접근할 수 있는지 확인합니다.
     *
     * @param notionAccessToken Notion 액세스 토큰
     * @param notionPageId      확인할 Notion 페이지 ID
     * @return 페이지 접근 가능 여부
     */
    @GetMapping("/check-page")
    @ResponseBody
    @Operation(
            summary = "Notion 페이지 접근 확인",
            description = "주어진 액세스 토큰으로 지정된 Notion 페이지에 접근할 수 있는지 확인합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "확인 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> checkNotionPage(
            @Parameter(description = "Notion 액세스 토큰", required = true)
            @RequestParam String notionAccessToken,
            @Parameter(description = "확인할 Notion 페이지 ID", required = true)
            @RequestParam String notionPageId) {

        try {
            log.info("Notion 페이지 접근 확인 요청 - 페이지 ID: {}", notionPageId);

            if (notionAccessToken == null || notionAccessToken.trim().isEmpty()) {
                return ResponseUtil.badRequest("Notion 액세스 토큰이 필요합니다.");
            }

            if (notionPageId == null || notionPageId.trim().isEmpty()) {
                return ResponseUtil.badRequest("Notion 페이지 ID가 필요합니다.");
            }

            // NotionService를 직접 주입받아 사용
            boolean pageExists = notionIntegrationService.checkPageExists(notionAccessToken, notionPageId);

            Map<String, Object> result = Map.of(
                    "accessible", pageExists,
                    "message", pageExists ? "페이지에 접근할 수 있습니다." : "페이지에 접근할 수 없습니다."
            );

            log.info("Notion 페이지 접근 확인 완료 - 페이지 ID: {}, 접근 가능: {}",
                    notionPageId, pageExists);

            return ResponseUtil.success(result);

        } catch (Exception e) {
            log.error("Notion 페이지 접근 확인 중 오류 발생", e);
            return ResponseUtil.internalServerError("확인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    /**
     * 노션 인증 코드를 액세스 토큰으로 교환
     * @param code 노션 인증 코드
     * @return 노션 액세스 토큰
     * @throws JsonProcessingException JSON 처리 오류
     */
    private String handleRedirectUrl(String code) throws JsonProcessingException {
        log.info("노션 토큰 교환 시작");

        NotionTokenRequester requester = new NotionTokenRequester(clientId, clientPw);
        requester.addParameter("grant_type", "authorization_code");
        requester.addParameter("code", code);
        requester.addParameter("redirect_uri", callBackUrl);

        String response = requester.requestToken("https://api.notion.com/v1/oauth/token");
        log.info("노션 토큰 응답 수신");

        Map<String, Object> jsonMap = new ObjectMapper().readValue(response, new TypeReference<Map<String, Object>>() {});
        String notionAccessToken = (String) jsonMap.get("access_token");

        if (notionAccessToken == null) {
            log.error("노션 액세스 토큰이 응답에 없음: {}", response);
            throw new RuntimeException("노션 액세스 토큰을 받을 수 없습니다.");
        }

        log.info("노션 액세스 토큰 획득 성공");
        return notionAccessToken;
    }
}