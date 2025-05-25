// src/main/java/com/ssafy/logoserver/domain/user/service/OAuth2UserService.java
package com.ssafy.logoserver.domain.user.service;

import com.ssafy.logoserver.domain.user.dto.OAuth2UserCompletionDto;
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

/**
 * OAuth2 사용자 관리 서비스
 * OAuth2 로그인 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
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

    /**
     * OAuth2User 정보를 기반으로 UserDto 반환
     * @param oAuth2User OAuth2 사용자 정보
     * @return 사용자 DTO (존재하지 않으면 null)
     */
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
     * 지원되는 OAuth2 제공자들의 로그인 URL을 맵으로 반환
     * @return OAuth2 제공자별 로그인 URL 맵
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
     * 최초 OAuth2 로그인 시 사용자 정보를 데이터베이스에 저장
     * @param oAuth2User OAuth2 사용자 정보
     * @return 저장된 사용자 DTO
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

        // 새 사용자 등록 (필수 정보 없이 초기 생성)
        User newUser = User.builder()
                .id(loginId)
                .name(name)
                .nickname(name) // 기본 닉네임으로 실명 사용 (추후 변경 필요)
                .email(email)
                .profileImage(pictureUrl)
                .provider(provider)
                .providerId(providerId)
                .role(User.Role.USER)
                // 필수 정보들은 null로 설정하여 추가 입력 필요 상태로 생성
                .gender(null)
                .birthday(null)
                .build();

        User savedUser = userRepository.save(newUser);
        return UserDto.fromEntity(savedUser);
    }

    /**
     * OAuth2 사용자 추가 정보 완성 (기존 메서드 - 하위 호환성 유지)
     * @deprecated 새로운 completeUserInfoWithCurrentUser 메서드 사용 권장
     */
    @Deprecated
    @Transactional
    public UserDto completeUserInfo(OAuth2UserCompletionDto completionDto) {
        // 기존 코드와 호환성을 위해 유지하되, 실제로는 사용되지 않을 예정
        log.warn("Deprecated completeUserInfo method called. Use completeUserInfoWithCurrentUser instead.");
        throw new UnsupportedOperationException("이 메서드는 더 이상 지원되지 않습니다. completeUserInfoWithCurrentUser를 사용하세요.");
    }

    /**
     * OAuth2 사용자 추가 정보 완성 (새로운 메서드)
     * 보안 컨텍스트에서 추출한 현재 사용자 ID를 사용하여 필수 정보 완성
     *
     * 완성되는 정보:
     * - nickname: 사용자가 입력한 닉네임 (필수, 중복 체크)
     * - gender: 성별 (필수)
     * - birthday: 생년월일 (필수)
     *
     * @param currentUserId 현재 로그인한 사용자 ID (보안 컨텍스트에서 추출)
     * @param completionDto 완성할 필수 정보 (nickname, gender, birthday)
     * @return 업데이트된 사용자 정보
     * @throws IllegalArgumentException 사용자가 존재하지 않거나 OAuth2 사용자가 아닌 경우
     */
    @Transactional
    public UserDto completeUserInfoWithCurrentUser(String currentUserId, OAuth2UserCompletionDto completionDto) {
        log.info("OAuth2 사용자 필수 정보 완성 시작 - 사용자 ID: {}", currentUserId);

        // 현재 사용자 조회
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + currentUserId));

        // OAuth2 사용자인지 확인
        if (user.getProvider() == null || user.getProviderId() == null) {
            throw new IllegalArgumentException("OAuth2 사용자가 아닙니다: " + currentUserId);
        }

        // 닉네임 중복 체크 (필수 입력이므로 항상 체크)
        if (completionDto.getNickname() != null &&
                !completionDto.getNickname().equals(user.getNickname()) &&
                userRepository.existsByNickname(completionDto.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다: " + completionDto.getNickname());
        }

        // 필수 정보 유효성 검증
        validateRequiredFields(completionDto);

        // 사용자 정보 업데이트 (필수 3개 필드만 업데이트)
        User updatedUser = User.builder()
                .uuid(user.getUuid())
                .id(user.getId())
                .password(user.getPassword())
                .name(user.getName())
                .email(user.getEmail())
                .nickname(completionDto.getNickname())       // 필수: 사용자 입력 닉네임
                .gender(completionDto.getGender())           // 필수: 성별
                .birthday(completionDto.getBirthday())       // 필수: 생년월일
                .profileImage(user.getProfileImage())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .role(user.getRole())
                .notionPageId(user.getNotionPageId())        // 기존 값 유지 (수정하지 않음)
                .created(user.getCreated())
                .build();

        User savedUser = userRepository.save(updatedUser);

        log.info("OAuth2 사용자 필수 정보 완성 완료 - 사용자 ID: {}, 닉네임: {}, 성별: {}, 생년월일: {}",
                currentUserId, savedUser.getNickname(), savedUser.getGender(), savedUser.getBirthday());

        return UserDto.fromEntity(savedUser);
    }

    /**
     * 필수 정보 유효성 검증 메서드
     * nickname, gender, birthday가 모두 입력되었는지 확인
     * @param completionDto 완성 정보 DTO
     * @throws IllegalArgumentException 필수 정보가 누락된 경우
     */
    private void validateRequiredFields(OAuth2UserCompletionDto completionDto) {
        if (completionDto.getNickname() == null || completionDto.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수 입력 항목입니다.");
        }

        if (completionDto.getGender() == null || completionDto.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("성별은 필수 입력 항목입니다.");
        }

        if (completionDto.getBirthday() == null) {
            throw new IllegalArgumentException("생년월일은 필수 입력 항목입니다.");
        }

        // 성별 값 유효성 검증
        String gender = completionDto.getGender().toUpperCase();
        if (!"M".equals(gender) && !"F".equals(gender) && !"O".equals(gender)) {
            throw new IllegalArgumentException("성별은 M(남성), F(여성), O(기타) 중 하나여야 합니다.");
        }

        log.debug("필수 정보 유효성 검증 완료 - 닉네임: {}, 성별: {}, 생년월일: {}",
                completionDto.getNickname(), completionDto.getGender(), completionDto.getBirthday());
    }

    /**
     * 사용자가 추가 정보 입력이 필요한지 확인 (UUID 기반)
     * OAuth2 로그인 후 필수 정보(nickname, gender, birthday) 입력 완료 여부 확인
     * @param userId 사용자 UUID
     * @return 추가 정보 입력 필요 여부
     */
    public boolean needsAdditionalInfo(Long userId) {
        Optional<User> userOptional = userRepository.findByUuid(userId);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        // 3개 필수 필드 중 하나라도 null이면 추가 정보 입력 필요
        boolean needsInfo = user.getGender() == null || user.getBirthday() == null;

        log.debug("사용자 추가 정보 필요 여부 확인 (UUID) - 사용자 UUID: {}, 성별: {}, 생년월일: {}, 필요 여부: {}",
                userId, user.getGender(), user.getBirthday(), needsInfo);

        return needsInfo;
    }

    /**
     * 사용자가 추가 정보 입력이 필요한지 확인 (로그인 ID 기반)
     * OAuth2 로그인 후 필수 정보(nickname, gender, birthday) 입력 완료 여부 확인
     * @param userId 사용자 로그인 ID
     * @return 추가 정보 입력 필요 여부
     */
    public boolean needsAdditionalInfo(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.warn("사용자를 찾을 수 없음: {}", userId);
            return false;
        }

        User user = userOptional.get();
        // 3개 필수 필드 중 하나라도 null이면 추가 정보 입력 필요
        boolean needsInfo = user.getGender() == null || user.getBirthday() == null;

        log.debug("사용자 추가 정보 필요 여부 확인 (로그인 ID) - 사용자 ID: {}, 성별: {}, 생년월일: {}, 필요 여부: {}",
                userId, user.getGender(), user.getBirthday(), needsInfo);

        return needsInfo;
    }

    /**
     * OAuth2 사용자인지 확인 (UUID 기반)
     * @param userId 사용자 UUID
     * @return OAuth2 사용자 여부
     */
    public boolean isOAuth2User(Long userId) {
        Optional<User> userOptional = userRepository.findByUuid(userId);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        return user.getProvider() != null && user.getProviderId() != null;
    }

    /**
     * OAuth2 사용자인지 확인 (로그인 ID 기반)
     * @param userId 사용자 로그인 ID
     * @return OAuth2 사용자 여부
     */
    public boolean isOAuth2User(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.warn("사용자를 찾을 수 없음: {}", userId);
            return false;
        }

        User user = userOptional.get();
        boolean isOAuth2 = user.getProvider() != null && user.getProviderId() != null;

        log.debug("OAuth2 사용자 여부 확인 - 사용자 ID: {}, 제공자: {}, OAuth2 사용자: {}",
                userId, user.getProvider(), isOAuth2);

        return isOAuth2;
    }
}