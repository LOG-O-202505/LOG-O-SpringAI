// src/main/java/com/ssafy/logoserver/security/oauth2/OAuth2AuthenticationSuccessHandler.java
package com.ssafy.logoserver.security.oauth2;

import com.ssafy.logoserver.domain.user.service.OAuth2UserService;
import com.ssafy.logoserver.security.jwt.JwtCookieProvider;
import com.ssafy.logoserver.security.jwt.TokenRotationService;
import com.ssafy.logoserver.utils.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    private final OAuth2UserService oAuth2UserService; // OAuth2UserService 의존성 추가

    // 허용된 리다이렉트 URI 목록
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

        // 추가 정보 쿠키 처리 (수정된 로직)
        handleAdditionalInfoCookie(request, response, authentication);

        log.info("OAuth2 authentication successful, redirecting to: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            log.error("Unauthorized redirect URI: {}", redirectUri.get());
            throw new IllegalArgumentException("Unauthorized redirect URI");
        }

        // 실제 데이터베이스 상태를 기반으로 온보딩 필요 여부 확인
        boolean needsOnboarding = checkIfUserNeedsOnboardingFromDatabase(authentication);
        String baseUrl = redirectUri.orElse(getDefaultTargetUrl());

        // 온보딩이 필요한 경우에만 파라미터 추가
        if (needsOnboarding) {
            String separator = baseUrl.contains("?") ? "&" : "?";
            return baseUrl + separator + "onboarding=true";
        }

        return baseUrl;
    }

    /**
     * 데이터베이스 상태를 기반으로 사용자가 온보딩이 필요한지 확인
     * OAuth2 속성이 아닌 실제 데이터베이스의 사용자 정보를 확인
     * @param authentication 인증 객체
     * @return 온보딩 필요 여부
     */
    private boolean checkIfUserNeedsOnboardingFromDatabase(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            return false;
        }

        try {
            // 인증된 사용자 이름(ID)을 가져옴
            String userId = authentication.getName();
            log.info("데이터베이스 기반 온보딩 필요 여부 확인 - 사용자 ID: {}", userId);

            // OAuth2UserService를 통해 실제 데이터베이스에서 추가 정보 필요 여부 확인
            boolean needsInfo = oAuth2UserService.needsAdditionalInfo(userId);

            log.info("데이터베이스 확인 결과 - 사용자 ID: {}, 추가 정보 필요: {}", userId, needsInfo);
            return needsInfo;

        } catch (Exception e) {
            log.error("온보딩 필요 여부 확인 중 오류 발생: {}", e.getMessage(), e);
            // 오류 발생 시 안전하게 false 반환 (온보딩 불필요로 처리)
            return false;
        }
    }

    /**
     * 추가 정보 쿠키 처리 로직 (수정됨)
     * 실제 데이터베이스 상태를 기반으로 쿠키 발급 여부 결정
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param authentication 인증 객체
     */
    private void handleAdditionalInfoCookie(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            return;
        }

        OAuth2User oAuth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 사용자 ID 추출
        Long userId = (Long) attributes.get("userId");
        String userIdString = authentication.getName();

        log.info("추가 정보 쿠키 처리 시작 - 사용자 ID: {}, UUID: {}", userIdString, userId);

        // 사용자 ID 쿠키는 OAuth2 사용자에게 항상 설정 (온보딩 페이지에서 필요)
        if (userId != null) {
            addOnboardingCookie(response, "user_id", userId.toString(), 24 * 60 * 60); // 24시간
            log.info("사용자 ID 쿠키 설정 완료 - userId: {}", userId);
        }

        // 실제 데이터베이스에서 추가 정보 필요 여부 확인
        try {
            boolean needsAdditionalInfo = oAuth2UserService.needsAdditionalInfo(userIdString);

            if (needsAdditionalInfo) {
                // 추가 정보가 필요한 경우에만 is_new_user 쿠키 설정
                addOnboardingCookie(response, "is_new_user", "true", 2 * 60); // 2분
                log.info("신규 사용자 쿠키 설정 완료 - 추가 정보 입력 필요");
            } else {
                // 추가 정보가 필요 없는 경우 is_new_user 쿠키 삭제
                clearOnboardingCookie(response, "is_new_user");
                log.info("기존 사용자 확인 - 추가 정보 불필요, is_new_user 쿠키 삭제");
            }

        } catch (Exception e) {
            log.error("추가 정보 필요 여부 확인 중 오류 발생: {}", e.getMessage(), e);
            // 오류 발생 시 안전하게 쿠키 삭제 처리
            clearOnboardingCookie(response, "is_new_user");
        }
    }

    /**
     * 온보딩 관련 쿠키 추가
     * @param response HTTP 응답
     * @param name 쿠키 이름
     * @param value 쿠키 값
     * @param maxAge 만료 시간 (초)
     */
    private void addOnboardingCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(false); // JavaScript 접근 허용 (온보딩 로직에서 필요)
        cookie.setSecure(false); // Production에서는 true로 설정 (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
        log.debug("온보딩 쿠키 추가 완료: {} = {}, 만료시간: {}초", name, value, maxAge);
    }

    /**
     * 온보딩 관련 쿠키 삭제
     * @param response HTTP 응답
     * @param name 쿠키 이름
     */
    private void clearOnboardingCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
        log.debug("온보딩 쿠키 삭제 완료: {}", name);
    }

    /**
     * 리다이렉트 URI 권한 확인
     * @param uri 확인할 URI
     * @return 권한 있는 URI 여부
     */
    private boolean isAuthorizedRedirectUri(String uri) {
        try {
            java.net.URI clientRedirectUri = java.net.URI.create(uri);
            return authorizedRedirectUris.stream()
                    .anyMatch(authorizedRedirectUri -> {
                        try {
                            java.net.URI authorizedURI = java.net.URI.create(authorizedRedirectUri);
                            return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                                    && authorizedURI.getPort() == clientRedirectUri.getPort();
                        } catch (Exception e) {
                            log.warn("허용된 리다이렉트 URI 파싱 오류: {}", authorizedRedirectUri, e);
                            return false;
                        }
                    });
        } catch (Exception e) {
            log.warn("리다이렉트 URI 파싱 오류: {}", uri, e);
            return false;
        }
    }

    /**
     * 인증 관련 속성 정리
     * @param request HTTP 요청
     * @param response HTTP 응답
     */
    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}