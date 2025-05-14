package com.ssafy.logoserver.domain.user.dto;

import com.ssafy.logoserver.domain.user.entity.User;
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
public class UserDto {
    private Long uuid;
    private String id;
    private String name;
    private String nickname;
    private LocalDate birthday;
    private String address;
    private String phone;
    private String profileImage;
    private User.Role role;
    private LocalDateTime created;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .uuid(user.getUuid())
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .birthday(user.getBirthday())
                .address(user.getAddress())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .created(user.getCreated())
                .build();
    }
}