package com.ssafy.logoserver.utils;

import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class SecurityUtil {

    private static UserRepository userRepository;

    @Autowired
    public SecurityUtil(UserRepository userRepository) {
        SecurityUtil.userRepository = userRepository;
    }

    /**
     * 현재 로그인한 사용자의 아이디를 반환
     * OAuth2와 일반 로그인을 모두 지원
     * @return 사용자 아이디
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("getCurrentUserId - authentication: {}", authentication);

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // OAuth2 로그인인 경우
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            log.info("getCurrentUserId - oauth2Token: {}", oauth2Token);
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
            log.info("getCurrentUserId - registrationId: {}", registrationId);
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            log.info("getCurrentUserId - oauth2User: {}", oauth2User);
            Map<String, Object> attributes = oauth2User.getAttributes();
            log.info("getCurrentUserId - attributes: {}", attributes);

            return extractUserIdFromOAuth2Attributes(registrationId, attributes);
        }

        // 일반 로그인인 경우
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        return String.valueOf(principal);
    }

    /**
     * OAuth2 제공자별 사용자 ID 추출
     * @param registrationId OAuth2 제공자 ID (google, naver, kakao)
     * @param attributes OAuth2 사용자 속성
     * @return 추출된 사용자 ID
     */
    private static String extractUserIdFromOAuth2Attributes(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "google":
                // Google의 경우 'sub' 필드가 사용자 ID
                log.info("extractUserIdFromOAuth2Attributes - attributes.get(\"sub\"): {}", attributes.get("sub"));
                return (String) attributes.get("sub");

            case "naver":
                // Naver의 경우 'response' 안의 'id' 필드가 사용자 ID
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                log.info("extractUserIdFromOAuth2Attributes - response: {}", response);
                log.info("extractUserIdFromOAuth2Attributes - response.get(\"id\"): {}", response.get("id"));
                if (response != null) {
                    return (String) response.get("id");
                }
                break;

            case "kakao":
                // Kakao의 경우 'id' 필드가 사용자 ID
                Object kakaoId = attributes.get("id");
                log.info("extractUserIdFromOAuth2Attributes - attributes.get(\"id\"): {}", kakaoId);
                if (kakaoId != null) {
                    return kakaoId.toString();
                }
                break;

            default:
                // 알 수 없는 제공자의 경우 attributes를 문자열로 반환 (기존 동작)
                return String.valueOf(attributes);
        }

        // 추출 실패시 null 반환
        return null;
    }

    /**
     * 현재 로그인한 사용자의 UUID 반환
     * @return 사용자 UUID
     */
    public static Long getCurrentUserUuid() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }

        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.map(User::getUuid).orElse(null);
    }

    /**
     * 현재 로그인한 사용자의 UUID와 주어진 UUID가 일치하는지 확인
     * @param uuid 비교할 UUID
     * @return 일치 여부
     */
    public static boolean isCurrentUserUuid(Long uuid) {
        Long currentUserUuid = getCurrentUserUuid();
        return currentUserUuid != null && currentUserUuid.equals(uuid);
    }

    /**
     * 현재 로그인한 사용자가 관리자인지 확인
     * @return 관리자 여부
     */
    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}