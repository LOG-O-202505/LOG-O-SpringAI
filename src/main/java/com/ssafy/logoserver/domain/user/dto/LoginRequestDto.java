package com.ssafy.logoserver.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청 DTO")
public class LoginRequestDto {

    @NotBlank(message = "ID는 필수 입력값입니다.")
    @Schema(description = "사용자 ID", example = "user123", required = true)
    private String id;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Schema(description = "비밀번호", example = "password123", required = true)
    private String password;
}