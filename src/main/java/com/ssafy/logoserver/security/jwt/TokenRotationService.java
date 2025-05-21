package com.ssafy.logoserver.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRotationService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenStore tokenStore;
    private final JwtCookieProvider cookieProvider;

    /**
     * 토큰 발급 (로그인 시)
     */
    public void issueTokens(HttpServletResponse response, Authentication authentication) {
        log.info("authentication: {}", authentication);
//        String userId = authentication.getName();

        // 사용자 ID 추출 로직
        String userId;
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Auth = (OAuth2AuthenticationToken) authentication;
            String registrationId = oauth2Auth.getAuthorizedClientRegistrationId();

            if ("naver".equalsIgnoreCase(registrationId)) {
                // 네이버 OAuth 처리
                Map<String, Object> attributes = oauth2Auth.getPrincipal().getAttributes();
                Map<String, Object> res = (Map<String, Object>) attributes.get("response");
                if (res != null && res.containsKey("id")) {
                    userId = "naver_" + res.get("id").toString();
                } else {
                    log.error("Naver OAuth res does not contain id field");
                    userId = oauth2Auth.getName(); // 백업 옵션
                }
            } else if ("google".equalsIgnoreCase(registrationId)) {
                // 구글 OAuth 처리
                Map<String, Object> attributes = oauth2Auth.getPrincipal().getAttributes();
                if (attributes.containsKey("sub")) {
                    userId = "google_" + attributes.get("sub").toString();
                } else {
                    userId = oauth2Auth.getName();
                }
            } else {
                // 다른 OAuth 제공자
                userId = oauth2Auth.getName();
            }
        } else {
            // 일반 인증
            userId = authentication.getName();
        }

        log.info("Token issued for user: {}", userId);
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // Redis에 리프레시 토큰 저장
        tokenStore.saveRefreshToken(userId, refreshToken, jwtTokenProvider.getRefreshTokenValidityInMilliseconds());

        // 응답 헤더에 액세스 토큰 설정
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Access-Control-Expose-Headers", "Authorization"); // 이 헤더 추가

        // 응답 쿠키에 리프레시 토큰 설정
        cookieProvider.addRefreshTokenCookie(response, refreshToken);
    }

    /**
     * OAuth2 사용자 토큰 발급 (JWT 토큰 반환)
     */
    public Map<String, String> issueOAuth2Tokens(Authentication authentication) {
        String userId = authentication.getName();
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // Redis에 리프레시 토큰 저장
        tokenStore.saveRefreshToken(userId, refreshToken, jwtTokenProvider.getRefreshTokenValidityInMilliseconds());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
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