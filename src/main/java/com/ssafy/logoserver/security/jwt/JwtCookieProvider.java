package com.ssafy.logoserver.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieProvider {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Value("${app.domain}")
    private String domain;

    /**
     * 리프레시 토큰을 HTTP Only 쿠키로 설정
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만 전송
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenValidity / 1000)); // 초 단위로 변환

        if (domain != null && !domain.isEmpty()) {
            cookie.setDomain(domain);
        }

        response.addCookie(cookie);
    }

    /**
     * 리프레시 토큰 쿠키 조회
     */
    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 리프레시 토큰 쿠키 삭제 (로그아웃 시)
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료

        if (domain != null && !domain.isEmpty()) {
            cookie.setDomain(domain);
        }

        response.addCookie(cookie);
    }
}