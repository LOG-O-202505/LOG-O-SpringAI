package com.ssafy.logoserver.domain.user.service;

import com.ssafy.logoserver.domain.user.dto.UserDto;
import com.ssafy.logoserver.domain.user.dto.UserRequestDto;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 모든 유저 조회
     * */
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * uuid(유저 고유 id)로 유저 조회
     * */
    public UserDto getUserByUid(Long uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + uuid));
        return UserDto.fromEntity(user);
    }

    /**
     * id로 유저 조회
     * */
    public UserDto getUserByLoginId(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 ID가 존재하지 않습니다: " + id));
        return UserDto.fromEntity(user);
    }

    /**
     * 닉네임으로 유저 조회
     * */
    public UserDto getUserByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임의 사용자가 존재하지 않습니다: " + nickname));
        return UserDto.fromEntity(user);
    }

    /**
     * 회원 가입
     * */
    @Transactional
    public UserDto createUser(UserRequestDto userRequestDto) {
        // ID 중복 체크
        if (userRepository.existsById(userRequestDto.getId())) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다: " + userRequestDto.getId());
        }

        // 이메일 중복 체크
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + userRequestDto.getEmail());
        }

        // 닉네임 중복 체크
        Optional<User> existingUserWithNickname = userRepository.findByNickname(userRequestDto.getNickname());
        if (existingUserWithNickname.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다: " + userRequestDto.getNickname());
        }

        // 비밀번호 암호화
        User user = userRequestDto.toEntity();
        user = User.builder()
                .id(user.getId())
                .password(passwordEncoder.encode(user.getPassword()))
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .nickname(user.getNickname())
                .birthday(user.getBirthday())
                .profileImage(user.getProfileImage())
                .role(user.getRole() != null ? user.getRole() : User.Role.USER)
                .build();

        return UserDto.fromEntity(userRepository.save(user));
    }

    /**
     * 유저 정보 수정
     * */
    @Transactional
    public UserDto updateUser(Long uuid, UserRequestDto userRequestDto) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + uuid));

        // 이메일 변경 시 중복 체크
        if (userRequestDto.getEmail() != null && !userRequestDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userRequestDto.getEmail())) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + userRequestDto.getEmail());
            }
        }

        // 닉네임 변경 시 중복 체크
        if (userRequestDto.getNickname() != null && !userRequestDto.getNickname().equals(user.getNickname())) {
            Optional<User> existingUserWithNickname = userRepository.findByNickname(userRequestDto.getNickname());
            if (existingUserWithNickname.isPresent()) {
                throw new IllegalArgumentException("이미 존재하는 닉네임입니다: " + userRequestDto.getNickname());
            }
        }

        // 기존 사용자 정보를 업데이트
        User updatedUser = User.builder()
                .uuid(user.getUuid())
                .id(user.getId()) // ID는 변경 불가
                .password(userRequestDto.getPassword() != null ?
                        passwordEncoder.encode(userRequestDto.getPassword()) :
                        user.getPassword())
                .name(userRequestDto.getName() != null ?
                        userRequestDto.getName() :
                        user.getName())
                .email(userRequestDto.getEmail() != null ?
                        userRequestDto.getEmail() :
                        user.getEmail())
                .gender(userRequestDto.getGender() != null ?
                        userRequestDto.getGender() :
                        user.getGender())
                .nickname(userRequestDto.getNickname() != null ?
                        userRequestDto.getNickname() :
                        user.getNickname())
                .birthday(userRequestDto.getBirthday() != null ?
                        userRequestDto.getBirthday() :
                        user.getBirthday())
                .profileImage(userRequestDto.getProfileImage() != null ?
                        userRequestDto.getProfileImage() :
                        user.getProfileImage())
                .role(userRequestDto.getRole() != null ?
                        userRequestDto.getRole() :
                        user.getRole())
                .created(user.getCreated())
                .build();

        return UserDto.fromEntity(userRepository.save(updatedUser));
    }

    /**
     * 유저 삭제 (회원 탈퇴)
     * */
    @Transactional
    public void deleteUser(Long uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + uuid));

        userRepository.delete(user);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * OAuth2 로그인을 처리하거나 업데이트 하는 메서드
     */
    @Transactional
    public UserDto processOAuthUser(String provider, String providerId, String email, String name, String profileImage) {
        // OAuth2 로그인 ID 생성 (provider_providerId 형식)
        String oAuthId = provider + "_" + providerId;

        Optional<User> existingUser = userRepository.findById(oAuthId);
        User user;

        if (existingUser.isPresent()) {
            // 기존 사용자 정보 업데이트
            user = existingUser.get();
            // 이메일, 이름, 프로필 이미지 업데이트
            User updatedUser = User.builder()
                    .uuid(user.getUuid())
                    .id(oAuthId)
                    .password(user.getPassword())
                    .name(name)
                    .nickname(user.getNickname())
                    .email(email)
                    .birthday(user.getBirthday())
                    .profileImage(profileImage)
                    .provider(provider)
                    .providerId(providerId)
                    .role(user.getRole())
                    .created(user.getCreated())
                    .build();

            user = userRepository.save(updatedUser);
        } else {
            // 새 사용자 등록
            User newUser = User.builder()
                    .id(oAuthId)
                    .name(name)
                    .nickname(name) // 닉네임은 이름으로 초기화
                    .email(email)
                    .profileImage(profileImage)
                    .provider(provider)
                    .providerId(providerId)
                    .role(User.Role.USER)
                    .build();

            user = userRepository.save(newUser);
        }

        return UserDto.fromEntity(user);
    }
}