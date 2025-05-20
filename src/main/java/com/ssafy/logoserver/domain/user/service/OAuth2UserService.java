package com.ssafy.logoserver.domain.user.service;

import com.ssafy.logoserver.domain.user.dto.UserDto;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OAuth2UserService {

    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${app.oauth2.authorize-uri:http://localhost:8080/oauth2/authorize}")
    private String authorizeUri;

    public UserDto getOAuth2User(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String provider = "google"; // OAuth2 제공자 정보를 추출하는 로직이 필요할 수 있음
        String providerId = attributes.get("sub").toString();
        String loginId = provider + "_" + providerId;

        Optional<User> userOptional = userRepository.findById(loginId);
        if (userOptional.isPresent()) {
            return UserDto.fromEntity(userOptional.get());
        }

        return null; // 사용자가 없는 경우
    }

    /**
     * OAuth2 로그인 URL 생성
     */
    public Map<String, String> getOAuth2LoginUrls() {
        Map<String, String> loginUrls = new HashMap<>();

        // Google 로그인 URL
        String googleLoginUrl = authorizeUri + "/google";
        loginUrls.put("google", googleLoginUrl);

        return loginUrls;
    }

    /**
     * OAuth2 사용자 정보 저장
     */
    @Transactional
    public UserDto saveOAuth2User(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String provider = "google";
        String providerId = attributes.get("sub").toString();
        String email = attributes.get("email").toString();
        String name = attributes.get("name").toString();
        String pictureUrl = attributes.getOrDefault("picture", "").toString();
        String loginId = provider + "_" + providerId;

        // 기존 사용자 확인
        Optional<User> existingUser = userRepository.findById(loginId);
        if (existingUser.isPresent()) {
            // 필요시 사용자 정보 업데이트 로직
            User user = existingUser.get();
            return UserDto.fromEntity(user);
        }

        // 새 사용자 등록
        User newUser = User.builder()
                .id(loginId)
                .name(name)
                .nickname(name) // 기본 닉네임으로 실명 사용
                .email(email)
                .profileImage(pictureUrl)
                .provider(provider)
                .providerId(providerId)
                .role(User.Role.USER)
                .build();

        User savedUser = userRepository.save(newUser);
        return UserDto.fromEntity(savedUser);
    }
}