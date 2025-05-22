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
        log.info("Checking onboarding status");

        // Check authentication cookies first - user must be authenticated
        boolean hasAuthCookies = hasAuthenticationCookies(request);
        if (!hasAuthCookies) {
            log.info("No authentication cookies found, user not authenticated");
            Map<String, Object> status = Map.of(
                    "isNewUser", false,
                    "userId", (Object) null
            );
            return ResponseUtil.success(status);
        }

        // Check onboarding information from cookies
        boolean isNewUser = getCookieValue(request, "is_new_user", "false").equals("true");
        String userIdStr = getCookieValue(request, "user_id", null);

        log.info("Onboarding cookies - isNewUser: {}, userId: {}", isNewUser, userIdStr);

        Map<String, Object> status = new HashMap<>();
        status.put("isNewUser", isNewUser);

        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                status.put("userId", userId);
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in cookie: {}", userIdStr);
                status.put("userId", null);
            }
        } else {
            status.put("userId", null);
        }

        log.info("Final onboarding status: {}", status);
        return ResponseUtil.success(status);
    }

    @PostMapping("/complete")
    @Operation(summary = "Complete OAuth2 User Information", description = "Complete required information after OAuth2 login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Information completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> completeOAuth2UserInfo(
            @Valid @RequestBody OAuth2UserCompletionDto completionDto,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            log.info("Completing OAuth2 user info for user: {}", completionDto.getUserId());

            // Update user information
            UserDto updatedUser = oAuth2UserService.completeUserInfo(completionDto);

            // Clear onboarding related cookies
            clearOnboardingCookies(response);

            log.info("OAuth2 user info completed successfully for user: {}", completionDto.getUserId());

            return ResponseUtil.success(updatedUser);

        } catch (IllegalArgumentException e) {
            log.error("Error completing OAuth2 user info: {}", e.getMessage());
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error completing OAuth2 user info: {}", e.getMessage(), e);
            return ResponseUtil.internalServerError("An error occurred while completing user information.");
        }
    }

    @GetMapping("/login-urls")
    @Operation(summary = "Get OAuth2 Login URLs", description = "Retrieve available OAuth2 login URLs.")
    public ResponseEntity<Map<String, Object>> getOAuth2LoginUrls() {
        Map<String, String> loginUrls = oAuth2UserService.getOAuth2LoginUrls();
        return ResponseUtil.success(loginUrls);
    }

    // Helper method to check if user has authentication cookies
    private boolean hasAuthenticationCookies(HttpServletRequest request) {
        String accessToken = getCookieValue(request, "access_token", null);
        return accessToken != null && !accessToken.trim().isEmpty();
    }

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

    private void clearOnboardingCookies(HttpServletResponse response) {
        log.info("Clearing onboarding cookies");
        clearCookie(response, "is_new_user");
        clearCookie(response, "user_id");
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setMaxAge(0); // Expire immediately
        response.addCookie(cookie);
        log.debug("Cleared cookie: {}", name);
    }
}