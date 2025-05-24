// src/main/java/com/ssafy/logoserver/controller/OAuth2UserController.java
package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.user.dto.OAuth2UserCompletionDto;
import com.ssafy.logoserver.domain.user.dto.UserDto;
import com.ssafy.logoserver.domain.user.service.OAuth2UserService;
import com.ssafy.logoserver.utils.ResponseUtil;
import com.ssafy.logoserver.utils.SecurityUtil;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth2 API", description = "OAuth2 authentication related API")
public class OAuth2UserController {

    private final OAuth2UserService oAuth2UserService;

    @GetMapping("/user")
    @Operation(summary = "Get OAuth2 User Info", description = "Retrieve current OAuth2 authenticated user information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Map<String, Object>> getOAuth2UserInfo(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "OAuth2 authentication required.");
        }

        UserDto userDto = oAuth2UserService.getOAuth2User(oAuth2User);
        return ResponseUtil.success(userDto);
    }

    @GetMapping("/onboarding/status")
    @Operation(summary = "Check Onboarding Status", description = "Check OAuth2 user onboarding status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getOnboardingStatus(HttpServletRequest request) {
        log.info("온보딩 상태 확인 시작");

        // 인증 쿠키 확인 - 사용자가 로그인되어 있어야 함
        boolean hasAuthCookies = hasAuthenticationCookies(request);
        if (!hasAuthCookies) {
            log.info("인증 쿠키 없음, 사용자 미인증 상태");
            Map<String, Object> status = Map.of(
                    "isNewUser", false,
                    "userId", (Object) null
            );
            return ResponseUtil.success(status);
        }

        // 쿠키에서 온보딩 정보 확인
        boolean isNewUser = getCookieValue(request, "is_new_user", "false").equals("true");
        String userIdStr = getCookieValue(request, "user_id", null);

        log.info("온보딩 쿠키 정보 - isNewUser: {}, userId: {}", isNewUser, userIdStr);

        Map<String, Object> status = new HashMap<>();
        status.put("isNewUser", isNewUser);

        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                status.put("userId", userId);
            } catch (NumberFormatException e) {
                log.warn("쿠키의 사용자 ID 형식이 잘못됨: {}", userIdStr);
                status.put("userId", null);
            }
        } else {
            status.put("userId", null);
        }

        log.info("최종 온보딩 상태: {}", status);
        return ResponseUtil.success(status);
    }

    /**
     * OAuth2 사용자 추가 정보 완성
     * 보안 컨텍스트에서 현재 로그인한 사용자 정보를 자동으로 추출하여 처리
     */
    @PostMapping("/complete")
    @PreAuthorize("isAuthenticated()") // 인증된 사용자만 접근 가능
    @Operation(summary = "Complete OAuth2 User Information",
            description = "Complete required information after OAuth2 login. User ID is automatically extracted from security context.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Information completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> completeOAuth2UserInfo(
            @Valid @RequestBody OAuth2UserCompletionDto completionDto,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // 보안 컨텍스트에서 현재 로그인한 사용자 ID 추출
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                log.error("보안 컨텍스트에서 사용자 ID를 가져올 수 없음");
                return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
            }

            log.info("OAuth2 사용자 정보 완성 시작 - 사용자 ID: {} (보안 컨텍스트에서 추출)", currentUserId);

            // 사용자 정보 업데이트 (현재 사용자 ID와 완성 정보를 함께 전달)
            UserDto updatedUser = oAuth2UserService.completeUserInfoWithCurrentUser(currentUserId, completionDto);

            // 온보딩 관련 쿠키 삭제 (OAuth2 추가 정보 완성 완료)
            clearOnboardingCookies(response);

            log.info("OAuth2 사용자 정보 완성 성공 - 사용자 ID: {}, 닉네임: {}",
                    currentUserId, updatedUser.getNickname());

            return ResponseUtil.success(updatedUser);

        } catch (IllegalArgumentException e) {
            log.error("OAuth2 사용자 정보 완성 실패: {}", e.getMessage());
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("OAuth2 사용자 정보 완성 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseUtil.internalServerError("사용자 정보 완성 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/login-urls")
    @Operation(summary = "Get OAuth2 Login URLs", description = "Retrieve available OAuth2 login URLs.")
    public ResponseEntity<Map<String, Object>> getOAuth2LoginUrls() {
        Map<String, String> loginUrls = oAuth2UserService.getOAuth2LoginUrls();
        return ResponseUtil.success(loginUrls);
    }

    /**
     * 사용자의 인증 쿠키가 있는지 확인하는 헬퍼 메서드
     * access_token 쿠키 존재 여부를 통해 로그인 상태를 확인
     * @param request HTTP 요청 객체
     * @return 인증 쿠키 존재 여부
     */
    private boolean hasAuthenticationCookies(HttpServletRequest request) {
        String accessToken = getCookieValue(request, "access_token", null);
        return accessToken != null && !accessToken.trim().isEmpty();
    }

    /**
     * 요청에서 특정 쿠키 값을 가져오는 헬퍼 메서드
     * @param request HTTP 요청 객체
     * @param name 쿠키 이름
     * @param defaultValue 기본값
     * @return 쿠키 값 (없으면 기본값)
     */
    private String getCookieValue(HttpServletRequest request, String name, String defaultValue) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
                }
            }
        }
        return defaultValue;
    }

    /**
     * 온보딩 관련 쿠키들을 삭제하는 헬퍼 메서드
     * OAuth2 사용자의 추가 정보 입력 완료 시 호출됨
     * @param response HTTP 응답 객체
     */
    private void clearOnboardingCookies(HttpServletResponse response) {
        log.info("온보딩 관련 쿠키 삭제 시작");

        // is_new_user 쿠키 삭제 (신규 유저 상태 표시 쿠키)
        clearCookie(response, "is_new_user");

        // user_id 쿠키 삭제 (온보딩용 임시 사용자 ID 쿠키)
        clearCookie(response, "user_id");

        log.info("온보딩 관련 쿠키 삭제 완료");
    }

    /**
     * 특정 쿠키를 삭제하는 헬퍼 메서드
     * 쿠키의 만료 시간을 0으로 설정하여 브라우저에서 즉시 삭제되도록 함
     * @param response HTTP 응답 객체
     * @param name 삭제할 쿠키 이름
     */
    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");                // 모든 경로에서 접근 가능
        cookie.setHttpOnly(false);          // JavaScript에서 접근 가능하도록 설정 (온보딩 로직에서 필요)
        cookie.setMaxAge(0);                // 즉시 만료 설정
        response.addCookie(cookie);
        log.debug("쿠키 삭제 완료: {}", name);
    }
}