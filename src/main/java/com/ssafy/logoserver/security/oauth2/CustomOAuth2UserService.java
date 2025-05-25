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

/**
 * 커스텀 OAuth2 사용자 서비스
 * OAuth2 인증 과정에서 사용자 정보를 처리하고 데이터베이스에 저장/업데이트하는 서비스
 * 필수 정보(nickname, gender, birthday) 누락 시 추가 입력이 필요한 상태로 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * OAuth2 사용자 정보를 로드하고 처리하는 메인 메서드
     * @param userRequest OAuth2 사용자 요청 정보
     * @return 처리된 OAuth2User 객체
     * @throws OAuth2AuthenticationException OAuth2 인증 실패 시
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 제공자: {}", registrationId);

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
     * OAuth2 사용자 정보를 처리하는 메서드
     * 신규 사용자 등록 또는 기존 사용자 정보 업데이트를 수행
     * 필수 정보 누락 시 추가 입력 필요 상태로 설정
     *
     * @param userRequest OAuth2 사용자 요청
     * @param oAuth2User OAuth2 사용자 정보
     * @return 처리된 OAuth2User 객체 (추가 속성 포함)
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        // 이메일 정보 필수 확인
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("OAuth2 제공자로부터 이메일 정보를 찾을 수 없습니다");
        }

        String providerId = oAuth2UserInfo.getId();
        String provider = oAuth2UserInfo.getProvider();
        String email = oAuth2UserInfo.getEmail();
        String loginId = oAuth2UserInfo.getId(); // providerId를 loginId로 직접 사용

        Optional<User> userOptional = userRepository.findById(providerId);
        User user;
        boolean isNewUser = false;

        if (userOptional.isPresent()) {
            // 기존 사용자 처리
            user = userOptional.get();
            log.info("기존 OAuth2 사용자 발견: {}", user.getId());

            // 기존 사용자 정보 업데이트 (프로필 이미지, 이름 등 기본 정보만)
            user = updateExistingUser(user, oAuth2UserInfo);

            // 기존 사용자도 필수 정보 누락 시 추가 입력 필요 상태로 처리
            boolean needsAdditionalInfo = checkNeedsAdditionalInfo(user);
            if (needsAdditionalInfo) {
                log.info("기존 사용자 필수 정보 누락으로 추가 입력 필요: {}", user.getId());
                isNewUser = true; // 온보딩 프로세스 진행을 위해 true로 설정
            }
        } else {
            // 신규 사용자 등록
            user = registerNewUser(oAuth2UserInfo);
            isNewUser = true;
            log.info("신규 OAuth2 사용자 등록: {}", user.getId());
        }

        // OAuth2User attributes에 추가 정보 포함
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("isNewUser", isNewUser);
        attributes.put("userId", user.getUuid());

        log.info("OAuth2 사용자 처리 완료 - isNewUser: {}, userId: {}", isNewUser, user.getUuid());

        // 제공자별 최종 attributes 및 nameAttributeKey 설정
        Map<String, Object> finalAttributes;
        String nameAttributeKey;

        if (registrationId.equalsIgnoreCase("naver")) {
            // Naver의 경우 response 객체 사용
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response != null) {
                // response 객체에 추가 정보 설정
                response.put("isNewUser", isNewUser);
                response.put("userId", user.getUuid());
                finalAttributes = response;
                nameAttributeKey = "id"; // Naver의 경우 response.id가 name attribute
            } else {
                // response가 null인 경우 전체 attributes 사용
                finalAttributes = attributes;
                nameAttributeKey = userRequest.getClientRegistration()
                        .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
            }
        } else {
            // Google, Kakao 등 기타 제공자
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
     * 제공자별 OAuth2UserInfo 객체 생성
     * @param registrationId OAuth2 제공자 ID
     * @param attributes 사용자 속성 정보
     * @return OAuth2UserInfo 구현체
     * @throws OAuth2AuthenticationException 지원하지 않는 제공자인 경우
     */
    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleUserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("naver")) {
            return new NaverUserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("kakao")) {
            return new KakaoUserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자: " + registrationId);
        }
    }

    /**
     * 신규 OAuth2 사용자 등록
     * 필수 정보(nickname, gender, birthday)를 null로 설정하여 추가 입력이 필요한 상태로 생성
     *
     * @param oAuth2UserInfo OAuth2 사용자 정보
     * @return 등록된 사용자 엔티티
     */
    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
        log.info("신규 OAuth2 사용자 등록 시작: {}", oAuth2UserInfo.getEmail());

        User user = User.builder()
                .id(oAuth2UserInfo.getId())                  // providerId를 ID로 직접 사용
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .nickname(oAuth2UserInfo.getName())          // 임시 닉네임 (추후 사용자가 변경 필요)
                .provider(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getId())
                .profileImage(oAuth2UserInfo.getImageUrl())
                .role(User.Role.USER)
                // 필수 정보들을 의도적으로 null로 설정하여 추가 입력 필요 상태 생성
                .gender(null)                                // 필수: 성별 (추가 입력 필요)
                .birthday(null)                              // 필수: 생년월일 (추가 입력 필요)
                .build();

        User savedUser = userRepository.save(user);
        log.info("신규 OAuth2 사용자 등록 완료 - ID: {}, 이메일: {}", savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }

    /**
     * 기존 OAuth2 사용자 정보 업데이트
     * 기본 정보(이름, 이메일, 프로필 이미지)만 업데이트하고 필수 정보는 유지
     *
     * @param user 기존 사용자 엔티티
     * @param oAuth2UserInfo OAuth2 사용자 정보
     * @return 업데이트된 사용자 엔티티
     */
    private User updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        log.info("기존 OAuth2 사용자 정보 업데이트: {}", oAuth2UserInfo.getEmail());

        User updatedUser = User.builder()
                .uuid(user.getUuid())
                .id(user.getId())
                .password(user.getPassword())
                .name(oAuth2UserInfo.getName())              // OAuth2에서 받은 최신 이름으로 업데이트
                .nickname(user.getNickname())                // 기존 닉네임 유지 (사용자가 설정한 값)
                .email(oAuth2UserInfo.getEmail())            // OAuth2에서 받은 최신 이메일로 업데이트
                .gender(user.getGender())                    // 기존 성별 유지
                .birthday(user.getBirthday())                // 기존 생년월일 유지
                .profileImage(oAuth2UserInfo.getImageUrl())  // OAuth2에서 받은 최신 프로필 이미지로 업데이트
                .provider(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getId())
                .role(user.getRole())
                .notionPageId(user.getNotionPageId())        // 기존 노션 페이지 ID 유지
                .created(user.getCreated())
                .build();

        User savedUser = userRepository.save(updatedUser);
        log.info("기존 OAuth2 사용자 정보 업데이트 완료 - ID: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * 사용자가 필수 정보 입력이 필요한지 확인하는 메서드
     * 필수 정보: nickname (의미있는 값), gender, birthday
     *
     * @param user 사용자 엔티티
     * @return 추가 정보 입력 필요 여부
     */
    private boolean checkNeedsAdditionalInfo(User user) {
        // 성별 또는 생년월일이 null인 경우 추가 정보 필요
        boolean genderMissing = user.getGender() == null;
        boolean birthdayMissing = user.getBirthday() == null;

        // 닉네임이 실명과 같은 경우도 추가 입력 필요 (사용자가 개인화된 닉네임 설정하도록)
        boolean needsPersonalizedNickname = user.getNickname() != null &&
                user.getNickname().equals(user.getName());

        boolean needsInfo = genderMissing || birthdayMissing || needsPersonalizedNickname;

        log.debug("필수 정보 확인 - 사용자: {}, 성별 누락: {}, 생년월일 누락: {}, 닉네임 개인화 필요: {}, 전체 필요: {}",
                user.getId(), genderMissing, birthdayMissing, needsPersonalizedNickname, needsInfo);

        return needsInfo;
    }
}