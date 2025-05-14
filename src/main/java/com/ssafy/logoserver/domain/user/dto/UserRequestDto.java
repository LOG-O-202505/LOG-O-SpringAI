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

    @Schema(description = "생년월일", example = "1990-01-01", required = true)
    private LocalDate birthday;

    @Schema(description = "주소", example = "서울특별시 강남구", required = true)
    private String address;

    @Schema(description = "전화번호", example = "010-1234-5678", required = true)
    private String phone;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImage;

    @Schema(description = "사용자 권한 (기본값: USER)", example = "USER")
    private User.Role role;

    public User toEntity() {
        return User.builder()
                .id(id)
                .password(password)
                .name(name)
                .nickname(nickname)
                .birthday(birthday)
                .address(address)
                .phone(phone)
                .profileImage(profileImage)
                .role(role != null ? role : User.Role.USER)
                .build();
    }
}   