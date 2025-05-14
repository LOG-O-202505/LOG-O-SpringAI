package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.user.dto.UserDto;
import com.ssafy.logoserver.domain.user.dto.UserRequestDto;
import com.ssafy.logoserver.domain.user.service.UserService;
import com.ssafy.logoserver.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseUtil.success(users);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Map<String, Object>> getUserByUuid(@PathVariable Long uuid) {
        try {
            UserDto user = userService.getUserById(uuid);
            return ResponseUtil.success(user);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserRequestDto userRequestDto) {
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
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long uuid,
            @RequestBody UserRequestDto userRequestDto) {
        try {
            UserDto updatedUser = userService.updateUser(uuid, userRequestDto);
            return ResponseUtil.success(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("사용자 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long uuid) {
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