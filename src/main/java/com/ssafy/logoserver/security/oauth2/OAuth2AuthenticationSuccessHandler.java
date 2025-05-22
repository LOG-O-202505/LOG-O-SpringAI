// src/main/java/com/ssafy/logoserver/security/oauth2/OAuth2AuthenticationSuccessHandler.java
package com.ssafy.logoserver.security.oauth2;

import com.ssafy.logoserver.security.jwt.JwtCookieProvider;
import com.ssafy.logoserver.security.jwt.TokenRotationService;
import com.ssafy.logoserver.utils.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ssafy.logoserver.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenRotationService tokenRotationService;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final JwtCookieProvider jwtCookieProvider;

    // 하드코딩된 리다이렉트 URI 목록 (설정 파일 의존성 제거)
    private final List<String> authorizedRedirectUris = Arrays.asList(
            "http://localhost:3000/oauth2/redirect",
            "http://localhost:8080/oauth2/redirect",
            "http://localhost:8090/oauth2/redirect/"
    );

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);

        // JWT 토큰 발급
        tokenRotationService.issueTokens(response, authentication);

        // OAuth2 사용자 정보에서 추가 정보 필요 여부 확인
        handleAdditionalInfoCookie(request, response, authentication);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            log.error("Unauthorized redirect URI: {}", redirectUri.get());
            throw new IllegalArgumentException("Unauthorized redirect URI");
        }

        // OAuth2 사용자 정보 확인
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oAuth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            boolean isNewUser = (Boolean) attributes.getOrDefault("isNewUser", false);

            // 추가 정보가 필요한 경우 온보딩 페이지로 리다이렉트
            if (isNewUser) {
                return redirectUri.orElse("/") + "?onboarding=true";
            }
        }

        return redirectUri.orElse(getDefaultTargetUrl());
    }

    private void handleAdditionalInfoCookie(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oAuth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            boolean isNewUser = (Boolean) attributes.getOrDefault("isNewUser", false);
            Long userId = (Long) attributes.get("userId");


            addOnboardingCookie(response, "user_id", userId.toString(), 7 * 24 * 60 * 60);
            if (isNewUser) {
                addOnboardingCookie(response, "is_new_user", "true", 60);
            }

        }
    }

    private void addOnboardingCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(false); // JavaScript에서 접근 가능하도록 설정
        cookie.setSecure(false); // 개발 환경에서는 false, 프로덕션에서는 true
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return authorizedRedirectUris.stream()
                .anyMatch(authorizedRedirectUri -> {
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort();
                });
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}