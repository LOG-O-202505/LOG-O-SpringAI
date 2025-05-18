package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.user.dto.UserDto;
import com.ssafy.logoserver.domain.user.dto.UserRequestDto;
import com.ssafy.logoserver.domain.user.service.UserService;
import com.ssafy.logoserver.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "모든 사용자 조회", description = "시스템에 등록된 모든 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseUtil.success(users);
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "UUID로 사용자 조회", description = "UUID로 특정 사용자의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getUserByUuid(
            @Parameter(description = "사용자 UUID", required = true)
            @PathVariable Long uuid) {
        try {
            UserDto user = userService.getUserByUid(uuid);
            return ResponseUtil.success(user);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "ID로 사용자 조회", description = "사용자 ID로 특정 사용자의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getUserById(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable String id) {
        try {
            UserDto user = userService.getUserByLoginId(id);
            return ResponseUtil.success(user);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/nickname/{nickname}")
    @Operation(summary = "닉네임으로 사용자 조회", description = "닉네임으로 특정 사용자의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getUserByNickname(
            @Parameter(description = "사용자 닉네임", required = true)
            @PathVariable String nickname) {
        try {
            UserDto user = userService.getUserByNickname(nickname);
            return ResponseUtil.success(user);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/exists/{nickname}")
    @Operation(summary = "닉네임 중복 체크", description = "해당 닉네임이 이미 존재하는지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Boolean>> existUserNicknameByNickname(
            @Parameter(description = "사용자 닉네임", required = true)
            @PathVariable String nickname) {
        boolean exists = userService.existsByNickname(nickname);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping
    @Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> createUser(
            @Parameter(description = "사용자 정보", required = true)
            @RequestBody UserRequestDto userRequestDto) {
        try {
            UserDto createdUser = userService.createUser(userRequestDto);
            return ResponseUtil.success(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("사용자 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "사용자 정보 수정", description = "UUID로 특정 사용자의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> updateUser(
            @Parameter(description = "사용자 UUID", required = true)
            @PathVariable Long uuid,
            @Parameter(description = "수정할 사용자 정보", required = true)
            @RequestBody UserRequestDto userRequestDto) {
        try {
            UserDto updatedUser = userService.updateUser(uuid, userRequestDto);
            return ResponseUtil.success(updatedUser);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("이미 존재하는 닉네임")) {
                return ResponseUtil.badRequest(e.getMessage());
            }
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("사용자 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "사용자 삭제", description = "UUID로 특정 사용자를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deleteUser(
            @Parameter(description = "사용자 UUID", required = true)
            @PathVariable Long uuid) {
        try {
            userService.deleteUser(uuid);
            return ResponseUtil.success();
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("사용자 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}