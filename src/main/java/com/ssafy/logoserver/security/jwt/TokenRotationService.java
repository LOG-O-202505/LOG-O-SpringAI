package com.ssafy.logoserver.security.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenRotationService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenStore tokenStore;
    private final JwtCookieProvider cookieProvider;

    /**
     * 토큰 발급 (로그인 시)
     */
    public void issueTokens(HttpServletResponse response, Authentication authentication) {
        String userId = authentication.getName();
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // Redis에 리프레시 토큰 저장
        tokenStore.saveRefreshToken(userId, refreshToken, jwtTokenProvider.getRefreshTokenValidityInMilliseconds());

        // 응답 헤더에 액세스 토큰 설정
        response.setHeader("Authorization", "Bearer " + accessToken);

        // 응답 쿠키에 리프레시 토큰 설정
        cookieProvider.addRefreshTokenCookie(response, refreshToken);
    }

    /**
     * 토큰 새로고침 (RTR 방식)
     */
    public boolean rotateTokens(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 리프레시 토큰 조회
        String refreshToken = cookieProvider.getRefreshTokenFromCookies(request);
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return false;
        }

        // 리프레시 토큰의 사용자 정보 조회
        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String userId = authentication.getName();

        // Redis에 저장된 토큰과 비교
        String storedToken = tokenStore.getRefreshToken(userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            // 저장된 토큰과 일치하지 않음 - 토큰 재사용 시도로 간주
            tokenStore.invalidateAllUserSessions(userId);
            return false;
        }

        // 새 토큰 발급 (토큰 로테이션)
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // Redis 업데이트 및 응답 설정
        tokenStore.saveRefreshToken(userId, newRefreshToken, jwtTokenProvider.getRefreshTokenValidityInMilliseconds());
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        cookieProvider.addRefreshTokenCookie(response, newRefreshToken);

        return true;
    }

    /**
     * 로그아웃
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieProvider.getRefreshTokenFromCookies(request);
        if (refreshToken != null) {
            Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
            tokenStore.deleteRefreshToken(authentication.getName());
        }

        cookieProvider.deleteRefreshTokenCookie(response);
    }
}