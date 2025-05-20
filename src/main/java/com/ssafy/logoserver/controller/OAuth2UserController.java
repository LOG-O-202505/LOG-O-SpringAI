package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.user.dto.UserDto;
import com.ssafy.logoserver.domain.user.service.OAuth2UserService;
import com.ssafy.logoserver.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/login-urls")
    @Operation(summary = "OAuth2 로그인 URL 조회", description = "사용 가능한 OAuth2 로그인 URL을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getOAuth2LoginUrls() {
        Map<String, String> loginUrls = oAuth2UserService.getOAuth2LoginUrls();
        return ResponseUtil.success(loginUrls);
    }
}