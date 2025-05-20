package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.user.dto.*;
import com.ssafy.logoserver.domain.user.service.UserService;
import com.ssafy.logoserver.security.jwt.JwtTokenProvider;
import com.ssafy.logoserver.utils.ResponseUtil;
import com.ssafy.logoserver.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "인증 관련 API")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 ID와 비밀번호로 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequestDto.getId(), loginRequestDto.getPassword());

            //사용자 로그인 인증 (AuthenticationManager를 통한 인증 -> 내부적으로 DB데이터와 조회하여 검증)
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            //인증된 사용자 정보를 현 스레드내 스프링 보안 context에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            //토큰 생성 (AccessToken + RefreshToken)
            TokenDto tokenDto = tokenProvider.generateToken(authentication);

            return ResponseUtil.success(tokenDto);
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
    public ResponseEntity<Map<String, Object>> refreshToken(@Valid @RequestBody TokenRefreshRequestDto tokenRefreshRequestDto) {
        try {
            // 리프레시 토큰 검증
            if (!tokenProvider.validateToken(tokenRefreshRequestDto.getRefreshToken())) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid refresh token");
            }

            // 사용자 정보 추출
            Authentication authentication = tokenProvider.getAuthentication(tokenRefreshRequestDto.getRefreshToken());

            // 새 토큰 발급
            String newAccessToken = tokenProvider.createAccessToken(authentication);
            String newRefreshToken = tokenProvider.createRefreshToken(authentication);

            TokenDto tokenDto = new TokenDto(newAccessToken, newRefreshToken);
            return ResponseUtil.success(tokenDto);
        } catch (Exception e) {
            return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "Failed to refresh token: " + e.getMessage());
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
}