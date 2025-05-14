package com.ssafy.logoserver.domain.user.dto;

import com.ssafy.logoserver.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {
    private String id;
    private String password;
    private String name;
    private String nickname;
    private LocalDate birthday;
    private String address;
    private String phone;
    private String profileImage;
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