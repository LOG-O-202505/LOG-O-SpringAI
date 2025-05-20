package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.user.dto.*;
import com.ssafy.logoserver.domain.user.service.UserService;
import com.ssafy.logoserver.security.jwt.JwtTokenProvider;
import com.ssafy.logoserver.security.jwt.TokenRotationService;
import com.ssafy.logoserver.utils.ResponseUtil;
import com.ssafy.logoserver.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "인증 관련 API")
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenRotationService tokenRotationService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 ID와 비밀번호로 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequestDto.getId(), loginRequestDto.getPassword());

            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 토큰 발급 및 설정
            tokenRotationService.issueTokens(response, authentication);

            return ResponseUtil.success("로그인 성공");
        } catch (Exception e) {
            return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "인증에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody UserRequestDto userRequestDto) {
        try {
            UserDto createdUser = userService.createUser(userRequestDto);
            return ResponseUtil.success(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("회원가입 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 액세스 토큰을 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "갱신 성공"),
            @ApiResponse(responseCode = "401", description = "토큰 검증 실패", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        boolean rotated = tokenRotationService.rotateTokens(request, response);

        if (rotated) {
            return ResponseUtil.success("토큰이 갱신되었습니다");
        } else {
            return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자를 로그아웃시키고 토큰을 무효화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("로그아웃 요청 받음");

        // 쿠키 정보 로깅
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("쿠키 발견: {} = {}", cookie.getName(), "값 있음");
            }
        } else {
            log.info("요청에 쿠키 없음");
        }

        // Headers 정보 로깅
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                log.info("헤더 발견: {} = {}", headerName, request.getHeader(headerName));
            }
        }

        try {
            tokenRotationService.logout(request, response);
            log.info("로그아웃 성공 처리됨");
            return ResponseUtil.success("로그아웃 성공");
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류: {}", e.getMessage(), e);
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "로그아웃 처리 중 오류 발생: " + e.getMessage());
        }
    }

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        try {
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "Not authenticated");
            }

            UserDto user = userService.getUserByLoginId(currentUserId);
            return ResponseUtil.success(user);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "토큰 검증", description = "현재 토큰의 유효성을 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유효한 토큰"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> validateToken() {
        // 인증되어 있다면 200 OK 반환
        return ResponseUtil.success();
    }

    @GetMapping("/oauth2/status")
    @Operation(summary = "OAuth2 로그인 상태 확인", description = "현재 OAuth2 로그인 상태를 확인합니다.")
    public ResponseEntity<Map<String, Object>> getOAuth2LoginStatus(HttpServletRequest request) {
        // 토큰 확인을 통해 로그인 상태 판단
        String token = getTokenFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            String currentUserId = authentication.getName();

            try {
                UserDto user = userService.getUserByLoginId(currentUserId);

                // OAuth2 로그인 된 사용자인지 확인
                boolean isOAuth2User = user.getId() != null &&
                        (user.getId().startsWith("google_") || user.getId().startsWith("naver_"));

                Map<String, Object> statusData = new HashMap<>();
                statusData.put("authenticated", true);
                statusData.put("oauth2User", isOAuth2User);
                statusData.put("provider", isOAuth2User ? user.getId().split("_")[0] : null);
                statusData.put("userInfo", user);

                return ResponseUtil.success(statusData);
            } catch (Exception e) {
                return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 정보 조회 중 오류가 발생했습니다.");
            }
        }

        return ResponseUtil.success(Map.of("authenticated", false, "oauth2User", false));
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}