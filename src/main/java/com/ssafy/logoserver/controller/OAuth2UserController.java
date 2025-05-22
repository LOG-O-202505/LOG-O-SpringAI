// src/main/java/com/ssafy/logoserver/controller/OAuth2UserController.java
package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.user.dto.OAuth2UserCompletionDto;
import com.ssafy.logoserver.domain.user.dto.UserDto;
import com.ssafy.logoserver.domain.user.service.OAuth2UserService;
import com.ssafy.logoserver.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth2 API", description = "OAuth2 인증 관련 API")
public class OAuth2UserController {

    private final OAuth2UserService oAuth2UserService;

    @GetMapping("/user")
    @Operation(summary = "OAuth2 사용자 정보 조회", description = "현재 OAuth2로 로그인한 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<Map<String, Object>> getOAuth2UserInfo(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "OAuth2 인증이 필요합니다.");
        }

        UserDto userDto = oAuth2UserService.getOAuth2User(oAuth2User);
        return ResponseUtil.success(userDto);
    }

    @GetMapping("/onboarding/status")
    @Operation(summary = "온보딩 상태 확인", description = "OAuth2 사용자의 온보딩 상태를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 조회 성공")
    })
    public ResponseEntity<Map<String, Object>> getOnboardingStatus(HttpServletRequest request) {
        // 쿠키에서 온보딩 정보 확인
        boolean needsAdditionalInfo = getCookieValue(request, "needs_additional_info", "false").equals("true");
        boolean isNewUser = getCookieValue(request, "is_new_user", "false").equals("true");
        String userId = getCookieValue(request, "user_id", null);

        Map<String, Object> status = Map.of(
                "needsAdditionalInfo", needsAdditionalInfo,
                "isNewUser", isNewUser,
                "userId", userId != null ? Long.parseLong(userId) : null
        );

        return ResponseUtil.success(status);
    }

    @PostMapping("/complete")
    @Operation(summary = "OAuth2 사용자 추가 정보 완성", description = "OAuth2 로그인 후 필요한 추가 정보를 입력합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정보 완성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<Map<String, Object>> completeOAuth2UserInfo(
            @Valid @RequestBody OAuth2UserCompletionDto completionDto,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // 사용자 정보 업데이트
            UserDto updatedUser = oAuth2UserService.completeUserInfo(completionDto);

            // 온보딩 관련 쿠키 삭제
            clearOnboardingCookies(response);

            log.info("OAuth2 user info completed for user: {}", completionDto.getUserId());

            return ResponseUtil.success(updatedUser);

        } catch (IllegalArgumentException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error completing OAuth2 user info: {}", e.getMessage(), e);
            return ResponseUtil.internalServerError("사용자 정보 완성 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/login-urls")
    @Operation(summary = "OAuth2 로그인 URL 조회", description = "사용 가능한 OAuth2 로그인 URL을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getOAuth2LoginUrls() {
        Map<String, String> loginUrls = oAuth2UserService.getOAuth2LoginUrls();
        return ResponseUtil.success(loginUrls);
    }

    private String getCookieValue(HttpServletRequest request, String name, String defaultValue) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return defaultValue;
    }

    private void clearOnboardingCookies(HttpServletResponse response) {
        clearCookie(response, "needs_additional_info");
        clearCookie(response, "is_new_user");
        clearCookie(response, "user_id");
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}