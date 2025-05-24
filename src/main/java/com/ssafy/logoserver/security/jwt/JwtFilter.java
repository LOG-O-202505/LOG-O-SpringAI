package com.ssafy.logoserver.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final TokenRotationService tokenRotationService;
    private final JwtCookieProvider cookieProvider;

    public JwtFilter(JwtTokenProvider tokenProvider, TokenRotationService tokenRotationService, JwtCookieProvider cookieProvider) {
        this.tokenProvider = tokenProvider;
        this.tokenRotationService = tokenRotationService;
        this.cookieProvider = cookieProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = resolveToken(request);

        if (StringUtils.hasText(jwt)) {
            try {
                if (tokenProvider.validateToken(jwt)) {
                    // JWT에서 Authentication 객체 복원
                    Authentication authentication = tokenProvider.getAuthentication(jwt);
                    // SecurityContextHolder에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set Authentication to security context for '{}', uri: {}", authentication.getName(), request.getRequestURI());
                }
            } catch (ExpiredJwtException e) {
                // 액세스 토큰 만료 - 리프레시 토큰으로 갱신 시도
                log.debug("Access token expired, attempting token rotation");
                boolean rotated = tokenRotationService.rotateTokens(request, response);
                if (rotated) {
                    // 새 액세스 토큰으로 인증 정보 설정
                    String newJwt = cookieProvider.getAccessTokenTokenFromCookies(request);
                    if (newJwt != null && tokenProvider.validateToken(newJwt)) {
                        Authentication authentication = tokenProvider.getAuthentication(newJwt);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Token rotated successfully for user: {}", authentication.getName());
                    }
                } else {
                    log.debug("Token rotation failed");
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}