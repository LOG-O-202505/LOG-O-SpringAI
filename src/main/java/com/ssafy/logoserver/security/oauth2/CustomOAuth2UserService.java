// src/main/java/com/ssafy/logoserver/security/oauth2/CustomOAuth2UserService.java
package com.ssafy.logoserver.security.oauth2;

import com.ssafy.logoserver.domain.user.dto.GoogleUserInfo;
import com.ssafy.logoserver.domain.user.dto.KakaoUserInfo;
import com.ssafy.logoserver.domain.user.dto.NaverUserInfo;
import com.ssafy.logoserver.domain.user.dto.OAuth2UserInfo;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//
//        try {
//            return processOAuth2User(userRequest, oAuth2User);
//        } catch (AuthenticationException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            log.error("Exception occurred while processing OAuth2 user", ex);
//            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
//        }

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 Provider: {}", registrationId);

        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            log.info("OAuth2User attributes: {}", oAuth2User.getAttributes());
            return processOAuth2User(userRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            log.error("AuthenticationException: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Exception occurred while processing OAuth2 user: {}", ex.getMessage(), ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        String providerId = oAuth2UserInfo.getId();
        String email = oAuth2UserInfo.getEmail();
        String loginId = oAuth2UserInfo.getProvider() + "_" + providerId;

        Optional<User> userOptional = userRepository.findById(loginId);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // 기존 사용자 정보 업데이트
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            // 새로운 사용자 생성
            user = registerNewUser(oAuth2UserInfo);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oAuth2User.getAttributes(),
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleUserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("naver")) {
            return new NaverUserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("kakao")) {
            return new KakaoUserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        }
    }

    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
        log.info("Registering new OAuth2 user: {}", oAuth2UserInfo.getEmail());

        User user = User.builder()
                .id(oAuth2UserInfo.getProvider() + "_" + oAuth2UserInfo.getId())
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .nickname(oAuth2UserInfo.getName())
                .provider(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getId())
                .profileImage(oAuth2UserInfo.getImageUrl())
                .role(User.Role.USER)
                .build();

        return userRepository.save(user);
    }

    private User updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        log.info("Updating existing OAuth2 user: {}", oAuth2UserInfo.getEmail());

        User updatedUser = User.builder()
                .uuid(user.getUuid())
                .id(user.getId())
                .password(user.getPassword())
                .name(oAuth2UserInfo.getName())
                .nickname(user.getNickname())
                .email(oAuth2UserInfo.getEmail())
                .birthday(user.getBirthday())
                .profileImage(oAuth2UserInfo.getImageUrl())
                .provider(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getId())
                .role(user.getRole())
                .created(user.getCreated())
                .build();

        return userRepository.save(updatedUser);
    }
}