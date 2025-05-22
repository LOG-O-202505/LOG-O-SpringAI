package com.ssafy.logoserver.domain.user.dto;

import com.ssafy.logoserver.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 DTO")
public class UserDto {

    @Schema(description = "사용자 UUID", example = "1")
    private Long uuid;

    @Schema(description = "사용자 ID", example = "user123")
    private String id;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일", example = "user123@example.com")
    private String email;

    @Schema(description = "성별", example = "M")
    private String gender;

    @Schema(description = "닉네임", example = "길동이")
    private String nickname;

    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate birthday;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImage;

    @Schema(description = "노션 페이지 ID", example = "12345abc-6789-def0-1234-56789abcdef0")
    private String notionPageId;

    @Schema(description = "OAuth2 제공자", example = "google")
    private String provider;

    @Schema(description = "사용자 권한", example = "USER")
    private User.Role role;

    @Schema(description = "계정 생성일", example = "2025-01-01T00:00:00")
    private LocalDateTime created;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .uuid(user.getUuid())
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .nickname(user.getNickname())
                .birthday(user.getBirthday())
                .profileImage(user.getProfileImage())
                .notionPageId(user.getNotionPageId())
                .provider(user.getProvider())
                .role(user.getRole())
                .created(user.getCreated())
                .build();
    }
}