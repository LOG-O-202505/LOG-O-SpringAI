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

    // Authorized redirect URIs
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

        // Issue JWT tokens first
        tokenRotationService.issueTokens(response, authentication);

        // Handle additional info cookie for OAuth2 users
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

        // Check if user needs onboarding
        boolean needsOnboarding = checkIfUserNeedsOnboarding(authentication);
        String baseUrl = redirectUri.orElse(getDefaultTargetUrl());

        // Add onboarding parameter only if needed
        if (needsOnboarding) {
            String separator = baseUrl.contains("?") ? "&" : "?";
            return baseUrl + separator + "onboarding=true";
        }

        return baseUrl;
    }

    private boolean checkIfUserNeedsOnboarding(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oAuth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            // Check if this is a new user that needs additional info
            Boolean isNewUser = (Boolean) attributes.get("isNewUser");

            log.info("OAuth2 user onboarding check - isNewUser: {}", isNewUser);
            return Boolean.TRUE.equals(isNewUser);
        }
        return false;
    }

    private void handleAdditionalInfoCookie(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oAuth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            Boolean isNewUser = (Boolean) attributes.get("isNewUser");
            Long userId = (Long) attributes.get("userId");

            log.info("Setting onboarding cookies - isNewUser: {}, userId: {}", isNewUser, userId);

            // Always set user_id cookie for OAuth2 users
            if (userId != null) {
                addOnboardingCookie(response, "user_id", userId.toString(), 24 * 60 * 60); // 24 hours
            }

            // Only set is_new_user cookie if user actually needs onboarding
            if (Boolean.TRUE.equals(isNewUser)) {
                addOnboardingCookie(response, "is_new_user", "true", 2 * 60); // 2 minutes
                log.info("Added is_new_user cookie for user requiring onboarding");
            } else {
                // Ensure the cookie is cleared if user doesn't need onboarding
                clearOnboardingCookie(response, "is_new_user");
                log.info("Cleared is_new_user cookie for user not requiring onboarding");
            }
        }
    }

    private void addOnboardingCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(false); // Allow JavaScript access for onboarding logic
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
        log.debug("Added onboarding cookie: {} = {}", name, value);
    }

    private void clearOnboardingCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Expire immediately
        response.addCookie(cookie);
        log.debug("Cleared onboarding cookie: {}", name);
    }

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
                            log.warn("Error parsing authorized redirect URI: {}", authorizedRedirectUri, e);
                            return false;
                        }
                    });
        } catch (Exception e) {
            log.warn("Error parsing redirect URI: {}", uri, e);
            return false;
        }
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}