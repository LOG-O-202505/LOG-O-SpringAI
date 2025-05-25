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
import java.util.HashMap;
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
            log.error("OAuth2 사용자 처리 중 예외 발생: {}", ex.getMessage(), ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * OAuth2 사용자 처리 로직
     * 사용자 등록/업데이트 및 추가 정보 필요 여부 결정
     * @param userRequest OAuth2 사용자 요청
     * @param oAuth2User OAuth2 사용자 정보
     * @return 처리된 OAuth2User 객체
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        // 이메일 정보 필수 검증
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("OAuth2 제공자로부터 이메일 정보를 가져올 수 없습니다");
        }

        String providerId = oAuth2UserInfo.getId();
        String provider = oAuth2UserInfo.getProvider();
        String email = oAuth2UserInfo.getEmail();

        Optional<User> userOptional = userRepository.findById(providerId);
        User user;
        boolean isNewUser = false; // 실제 신규 사용자 여부

        if (userOptional.isPresent()) {
            // 기존 사용자 - 정보 업데이트
            user = userOptional.get();
            log.info("기존 OAuth2 사용자 발견: {}", user.getId());

            user = updateExistingUser(user, oAuth2UserInfo);

            // 기존 사용자도 필수 정보가 없으면 신규 사용자로 처리하지 않음
            // isNewUser는 실제로 처음 가입하는 사용자에게만 true로 설정
            isNewUser = false;

            log.info("기존 사용자 정보 업데이트 완료 - ID: {}, 추가정보필요: {}",
                    user.getId(), needsAdditionalInfo(user));
        } else {
            // 신규 사용자 등록
            user = registerNewUser(oAuth2UserInfo);
            isNewUser = true; // 실제 신규 사용자
            log.info("신규 OAuth2 사용자 등록 완료: {}", user.getId());
        }

        // OAuth2User 속성에 사용자 정보 추가
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("isNewUser", isNewUser); // 실제 신규 사용자 여부만 설정
        attributes.put("userId", user.getUuid());
        attributes.put("needsAdditionalInfo", needsAdditionalInfo(user)); // 추가 정보 필요 여부

        log.info("OAuth2 사용자 처리 완료 - isNewUser: {}, userId: {}, needsAdditionalInfo: {}",
                isNewUser, user.getUuid(), needsAdditionalInfo(user));

        // 최종 OAuth2User 객체 생성
        Map<String, Object> finalAttributes;
        String nameAttributeKey;

        if (registrationId.equalsIgnoreCase("naver")) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response != null) {
                // Naver의 response 객체에 추가 정보 설정
                response.put("isNewUser", isNewUser);
                response.put("userId", user.getUuid());
                response.put("needsAdditionalInfo", needsAdditionalInfo(user));
                finalAttributes = response;
                nameAttributeKey = "id"; // Naver는 response.id를 name attribute로 사용
            } else {
                // response가 null인 경우 전체 attributes 사용
                finalAttributes = attributes;
                nameAttributeKey = userRequest.getClientRegistration()
                        .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
            }
        } else {
            finalAttributes = attributes;
            nameAttributeKey = userRequest.getClientRegistration()
                    .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                finalAttributes,
                nameAttributeKey
        );
    }

    /**
     * OAuth2 제공자별 사용자 정보 객체 생성
     * @param registrationId OAuth2 제공자 ID
     * @param attributes 사용자 속성
     * @return OAuth2UserInfo 구현 객체
     */
    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleUserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("naver")) {
            return new NaverUserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("kakao")) {
            return new KakaoUserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
        }
    }

    /**
     * 신규 OAuth2 사용자 등록
     * 필수 정보(gender, birthday)는 의도적으로 null로 설정하여 온보딩 유도
     * @param oAuth2UserInfo OAuth2 사용자 정보
     * @return 등록된 사용자 엔티티
     */
    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
        log.info("신규 OAuth2 사용자 등록 시작: {}", oAuth2UserInfo.getEmail());

        User user = User.builder()
                .id(oAuth2UserInfo.getId()) // providerId를 직접 사용
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .nickname(oAuth2UserInfo.getName()) // 초기 닉네임은 이름으로 설정
                .provider(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getId())
                .profileImage(oAuth2UserInfo.getImageUrl())
                .role(User.Role.USER)
                // 온보딩을 위해 의도적으로 null로 설정
                .gender(null)
                .birthday(null)
                .build();

        User savedUser = userRepository.save(user);
        log.info("신규 사용자 등록 완료 - ID: {}, 이메일: {}", savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }

    /**
     * 기존 OAuth2 사용자 정보 업데이트
     * 기존 사용자의 필수 정보는 유지하되, OAuth2에서 가져온 최신 정보로 업데이트
     * @param user 기존 사용자 엔티티
     * @param oAuth2UserInfo OAuth2 사용자 정보
     * @return 업데이트된 사용자 엔티티
     */
    private User updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        log.info("기존 OAuth2 사용자 정보 업데이트 시작: {}", oAuth2UserInfo.getEmail());

        User updatedUser = User.builder()
                .uuid(user.getUuid())
                .id(user.getId())
                .password(user.getPassword())
                .name(oAuth2UserInfo.getName()) // OAuth2에서 가져온 최신 이름으로 업데이트
                .nickname(user.getNickname()) // 기존 닉네임 유지
                .email(oAuth2UserInfo.getEmail()) // OAuth2에서 가져온 최신 이메일로 업데이트
                .gender(user.getGender()) // 기존 성별 유지
                .birthday(user.getBirthday()) // 기존 생년월일 유지
                .profileImage(oAuth2UserInfo.getImageUrl()) // OAuth2에서 가져온 최신 프로필 이미지로 업데이트
                .provider(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getId())
                .role(user.getRole())
                .notionPageId(user.getNotionPageId()) // 기존 노션 페이지 ID 유지
                .created(user.getCreated())
                .build();

        User savedUser = userRepository.save(updatedUser);
        log.info("기존 사용자 정보 업데이트 완료 - ID: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * 사용자가 추가 정보 입력이 필요한지 확인
     * gender 또는 birthday가 null인 경우 추가 정보 필요
     * @param user 사용자 엔티티
     * @return 추가 정보 필요 여부
     */
    private boolean needsAdditionalInfo(User user) {
        boolean needs = (user.getGender() == null || user.getBirthday() == null);
        log.debug("추가 정보 필요 여부 확인 - 사용자 ID: {}, 성별: {}, 생년월일: {}, 필요: {}",
                user.getId(), user.getGender(), user.getBirthday(), needs);
        return needs;
    }
}