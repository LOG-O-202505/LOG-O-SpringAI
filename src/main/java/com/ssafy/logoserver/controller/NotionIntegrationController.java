package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.notion.dto.NotionIntegrationDto;
import com.ssafy.logoserver.service.NotionIntegrationService;
import com.ssafy.logoserver.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Notion AI 통합 컨트롤러
 * AI 분석 후 Notion 페이지에 결과를 작성하는 통합 API를 제공
 */
@RestController
@RequestMapping("/api/notion")
@RequiredArgsConstructor
@Tag(name = "Notion Integration API", description = "AI 분석 결과를 Notion에 작성하는 통합 API")
@Slf4j
public class NotionIntegrationController {

    private final NotionIntegrationService notionIntegrationService;

    /**
     * AI 분석 후 Notion 페이지에 결과 작성
     * 사용자의 질문을 AI로 분석한 후, 그 결과를 지정된 Notion 페이지에 자동으로 작성합니다.
     *
     * @param request AI 질문, Notion 액세스 토큰, 페이지 ID를 포함한 요청 DTO
     * @return AI 분석 결과와 Notion 작성 성공 여부
     */
    @PostMapping("/ai-to-notion")
    @Operation(
            summary = "AI 분석 후 Notion 페이지 작성",
            description = "사용자의 질문을 AI로 분석한 후, 그 결과를 지정된 Notion 페이지에 자동으로 작성합니다. " +
                    "여행 일정 분석, 계획 수립 등의 용도로 사용할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "처리 성공 (AI 분석 및 Notion 작성 결과 포함)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 파라미터 누락 등)", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> processAiToNotion(
            @Parameter(description = "AI 질문, Notion 액세스 토큰, 페이지 ID를 포함한 요청", required = true)
            @Valid @RequestBody NotionIntegrationDto request) {

        try {
            log.info("AI-Notion 통합 요청 수신 - 질문 길이: {} 글자",
                    request.getQuestion() != null ? request.getQuestion().length() : 0);

            // 필수 파라미터 검증
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                return ResponseUtil.badRequest("질문 내용이 필요합니다.");
            }

            if (request.getNotionAccessToken() == null || request.getNotionAccessToken().trim().isEmpty()) {
                return ResponseUtil.badRequest("Notion 액세스 토큰이 필요합니다.");
            }

            if (request.getNotionPageId() == null || request.getNotionPageId().trim().isEmpty()) {
                return ResponseUtil.badRequest("Notion 페이지 ID가 필요합니다.");
            }

            // AI 분석 및 Notion 작성 처리
            NotionIntegrationDto.Response response = notionIntegrationService.processAiToNotion(request);

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
}