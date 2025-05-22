package com.ssafy.logoserver.domain.user.dto;

import com.ssafy.logoserver.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 요청 DTO")
public class UserRequestDto {

    @Schema(description = "사용자 ID", example = "user123", required = true)
    private String id;

    @Schema(description = "비밀번호", example = "password123", required = true)
    private String password;

    @Schema(description = "이름", example = "홍길동", required = true)
    private String name;

    @Schema(description = "닉네임", example = "길동이", required = true)
    private String nickname;

    @Schema(description = "이메일", example = "user123@example.com", required = true)
    private String email;

    @Schema(description = "성별", example = "M", required = true)
    private String gender;

    @Schema(description = "생년월일", example = "1990-01-01", required = true)
    private LocalDate birthday;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImage;

    @Schema(description = "사용자 권한 (기본값: USER)", example = "USER")
    private User.Role role;

    public User toEntity() {
        return User.builder()
                .id(id)
                .password(password)
                .name(name)
                .email(email)
                .gender(gender)
                .nickname(nickname)
                .birthday(birthday)
                .profileImage(profileImage)
                .role(role != null ? role : User.Role.USER)
                .build();
    }
}