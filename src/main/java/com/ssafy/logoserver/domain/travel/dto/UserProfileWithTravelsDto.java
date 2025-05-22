package com.ssafy.logoserver.domain.user.dto;

import com.ssafy.logoserver.domain.travel.dto.TravelDto;
import com.ssafy.logoserver.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 프로필과 여행 정보가 포함된 DTO")
public class UserProfileWithTravelsDto {

    @Schema(description = "사용자 UUID", example = "1")
    private Long uuid;

    @Schema(description = "사용자 ID", example = "user123")
    private String id;

    @Schema(description = "닉네임", example = "길동이")
    private String nickname;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate birthday;

    @Schema(description = "성별", example = "M")
    private String gender;

    @Schema(description = "이메일", example = "user123@example.com")
    private String email;

    @Schema(description = "사용자 권한", example = "USER")
    private User.Role role;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImage;

    @Schema(description = "계정 생성일", example = "2025-01-01T00:00:00")
    private LocalDateTime created;

    @Schema(description = "사용자의 여행 목록")
    private List<TravelDto> travels;

    public static UserProfileWithTravelsDto fromUserAndTravels(User user, List<TravelDto> travels) {
        return UserProfileWithTravelsDto.builder()
                .uuid(user.getUuid())
                .id(user.getId())
                .nickname(user.getNickname())
                .name(user.getName())
                .birthday(user.getBirthday())
                .gender(user.getGender())
                .email(user.getEmail())
                .role(user.getRole())
                .profileImage(user.getProfileImage())
                .created(user.getCreated())
                .travels(travels)
                .build();
    }
}