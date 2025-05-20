package com.ssafy.logoserver.security.jwt;

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

    public JwtFilter(JwtTokenProvider tokenProvider, TokenRotationService tokenRotationService) {
        this.tokenProvider = tokenProvider;
        this.tokenRotationService = tokenRotationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = resolveToken(request);

        if (StringUtils.hasText(jwt)) {
            if (tokenProvider.validateToken(jwt)) {
                // 액세스 토큰 유효 - 인증 정보 설정
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set Authentication to security context for '{}', uri: {}", authentication.getName(), request.getRequestURI());
            } else {
                // 액세스 토큰 만료 - 리프레시 토큰으로 갱신 시도
                boolean rotated = tokenRotationService.rotateTokens(request, response);
                if (rotated) {
                    // 토큰 갱신 성공 - 새 액세스 토큰의 인증 정보 설정
                    String newJwt = response.getHeader(AUTHORIZATION_HEADER).substring(BEARER_PREFIX.length());
                    Authentication authentication = tokenProvider.getAuthentication(newJwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Rotated tokens and set Authentication for user '{}', uri: {}", authentication.getName(), request.getRequestURI());
                } else {
                    log.debug("Invalid JWT token or failed to rotate tokens, uri: {}", request.getRequestURI());
                }
            }
        } else {
            log.debug("No JWT token found in request, uri: {}", request.getRequestURI());
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