package com.ssafy.logoserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Notion API 통신 서비스
 * Notion 페이지에 AI 분석 결과를 작성하는 기능을 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String NOTION_API_BASE_URL = "https://api.notion.com/v1";
    private static final String NOTION_VERSION = "2022-06-28";

    /**
     * Notion 페이지에 AI 분석 결과를 작성
     * @param accessToken 사용자의 Notion 액세스 토큰
     * @param pageId 대상 페이지 ID
     * @param content AI가 생성한 여행 분석 내용
     * @return 작성 성공 여부
     */
    public boolean writeToNotionPage(String accessToken, String pageId, String content) {
        try {
            log.info("Notion 페이지에 내용 작성 시작 - pageId: {}", pageId);

            // 마크다운 내용을 Notion 블록으로 변환
            List<Map<String, Object>> blocks = convertMarkdownToNotionBlocks(content);

            // Notion API 요청
            String url = NOTION_API_BASE_URL + "/blocks/" + pageId + "/children";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Notion-Version", NOTION_VERSION);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("children", blocks);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PATCH, requestEntity, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Notion 페이지 작성 성공 - pageId: {}", pageId);
                return true;
            } else {
                log.error("Notion 페이지 작성 실패 - 상태코드: {}, 응답: {}",
                        response.getStatusCode(), response.getBody());
                return false;
            }

        } catch (Exception e) {
            log.error("Notion 페이지 작성 중 오류 발생 - pageId: {}", pageId, e);
            return false;
        }
    }

    /**
     * 마크다운 텍스트를 Notion 블록으로 변환
     * @param content 마크다운 형식의 텍스트
     * @return Notion 블록 리스트
     */
    private List<Map<String, Object>> convertMarkdownToNotionBlocks(String content) {
        List<Map<String, Object>> blocks = new ArrayList<>();

        // 현재 시간을 포함한 제목 블록 추가
        blocks.add(createHeadingBlock("🎯 AI 여행 분석 결과 - " +
                java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                )));

        // 구분선 추가
        blocks.add(createDividerBlock());

        String[] lines = content.split("\n");
        StringBuilder paragraphText = new StringBuilder();

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty()) {
                // 빈 줄이면 현재까지의 문단을 블록으로 추가
                if (paragraphText.length() > 0) {
                    blocks.add(createParagraphBlock(paragraphText.toString()));
                    paragraphText = new StringBuilder();
                }
            } else if (line.startsWith("# ")) {
                // H1 헤딩
                if (paragraphText.length() > 0) {
                    blocks.add(createParagraphBlock(paragraphText.toString()));
                    paragraphText = new StringBuilder();
                }
                blocks.add(createHeadingBlock(line.substring(2)));
            } else if (line.startsWith("## ")) {
                // H2 헤딩
                if (paragraphText.length() > 0) {
                    blocks.add(createParagraphBlock(paragraphText.toString()));
                    paragraphText = new StringBuilder();
                }
                blocks.add(createHeading2Block(line.substring(3)));
            } else if (line.startsWith("### ")) {
                // H3 헤딩
                if (paragraphText.length() > 0) {
                    blocks.add(createParagraphBlock(paragraphText.toString()));
                    paragraphText = new StringBuilder();
                }
                blocks.add(createHeading3Block(line.substring(4)));
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                // 불릿 포인트
                if (paragraphText.length() > 0) {
                    blocks.add(createParagraphBlock(paragraphText.toString()));
                    paragraphText = new StringBuilder();
                }
                blocks.add(createBulletedListBlock(line.substring(2)));
            } else if (line.matches("^\\d+\\.\\s.*")) {
                // 번호 리스트
                if (paragraphText.length() > 0) {
                    blocks.add(createParagraphBlock(paragraphText.toString()));
                    paragraphText = new StringBuilder();
                }
                blocks.add(createNumberedListBlock(line.replaceFirst("^\\d+\\.\\s", "")));
            } else {
                // 일반 텍스트
                if (paragraphText.length() > 0) {
                    paragraphText.append("\n");
                }
                paragraphText.append(line);
            }
        }

        // 마지막 문단 처리
        if (paragraphText.length() > 0) {
            blocks.add(createParagraphBlock(paragraphText.toString()));
        }

        // 마지막에 구분선과 생성 시간 추가
        blocks.add(createDividerBlock());
        blocks.add(createParagraphBlock("📅 생성 시간: " +
                java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")
                )));

        return blocks;
    }

    /**
     * H1 헤딩 블록 생성
     */
    private Map<String, Object> createHeadingBlock(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("object", "block");
        block.put("type", "heading_1");

        Map<String, Object> heading = new HashMap<>();
        heading.put("rich_text", createRichTextArray(text));
        block.put("heading_1", heading);

        return block;
    }

    /**
     * H2 헤딩 블록 생성
     */
    private Map<String, Object> createHeading2Block(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("object", "block");
        block.put("type", "heading_2");

        Map<String, Object> heading = new HashMap<>();
        heading.put("rich_text", createRichTextArray(text));
        block.put("heading_2", heading);

        return block;
    }

    /**
     * H3 헤딩 블록 생성
     */
    private Map<String, Object> createHeading3Block(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("object", "block");
        block.put("type", "heading_3");

        Map<String, Object> heading = new HashMap<>();
        heading.put("rich_text", createRichTextArray(text));
        block.put("heading_3", heading);

        return block;
    }

    /**
     * 문단 블록 생성
     */
    private Map<String, Object> createParagraphBlock(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("object", "block");
        block.put("type", "paragraph");

        Map<String, Object> paragraph = new HashMap<>();
        paragraph.put("rich_text", createRichTextArray(text));
        block.put("paragraph", paragraph);

        return block;
    }

    /**
     * 불릿 리스트 블록 생성
     */
    private Map<String, Object> createBulletedListBlock(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("object", "block");
        block.put("type", "bulleted_list_item");

        Map<String, Object> listItem = new HashMap<>();
        listItem.put("rich_text", createRichTextArray(text));
        block.put("bulleted_list_item", listItem);

        return block;
    }

    /**
     * 번호 리스트 블록 생성
     */
    private Map<String, Object> createNumberedListBlock(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("object", "block");
        block.put("type", "numbered_list_item");

        Map<String, Object> listItem = new HashMap<>();
        listItem.put("rich_text", createRichTextArray(text));
        block.put("numbered_list_item", listItem);

        return block;
    }

    /**
     * 구분선 블록 생성
     */
    private Map<String, Object> createDividerBlock() {
        Map<String, Object> block = new HashMap<>();
        block.put("object", "block");
        block.put("type", "divider");
        block.put("divider", new HashMap<>());

        return block;
    }

    /**
     * 리치 텍스트 배열 생성
     */
    private List<Map<String, Object>> createRichTextArray(String text) {
        List<Map<String, Object>> richTextArray = new ArrayList<>();

        Map<String, Object> richText = new HashMap<>();
        richText.put("type", "text");

        Map<String, Object> textObject = new HashMap<>();
        textObject.put("content", text);
        richText.put("text", textObject);

        richTextArray.add(richText);

        return richTextArray;
    }

    /**
     * Notion 페이지 존재 여부 확인
     * @param accessToken 사용자의 Notion 액세스 토큰
     * @param pageId 확인할 페이지 ID
     * @return 페이지 존재 여부
     */
    public boolean checkPageExists(String accessToken, String pageId) {
        try {
            String url = NOTION_API_BASE_URL + "/pages/" + pageId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Notion-Version", NOTION_VERSION);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, String.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Notion 페이지 존재 확인 중 오류 발생 - pageId: {}", pageId, e);
            return false;
        }
    }
}