package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.image.dto.TravelImageDto;
import com.ssafy.logoserver.domain.image.service.TravelImageService;
import com.ssafy.logoserver.domain.travel.dto.TravelDto;
import com.ssafy.logoserver.domain.travel.service.TravelService;
import com.ssafy.logoserver.domain.user.dto.UserDto;
import com.ssafy.logoserver.domain.user.dto.UserProfileUpdateDto;
import com.ssafy.logoserver.domain.user.dto.UserProfileWithTravelsDto;
import com.ssafy.logoserver.domain.user.dto.UserRequestDto;
import com.ssafy.logoserver.domain.user.service.UserLikeService;
import com.ssafy.logoserver.domain.user.service.UserService;
import com.ssafy.logoserver.utils.ResponseUtil;
import com.ssafy.logoserver.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 관리 API")
@Slf4j
public class UserController {

    private final UserService userService;
    private final TravelService travelService;
    private final TravelImageService travelImageService;
    private final UserLikeService userLikeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "모든 사용자 조회", description = "시스템에 등록된 모든 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
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

    @GetMapping("/{uuid}/travels")
    @Operation(summary = "사용자의 여행 목록 조회", description = "특정 사용자가 작성한 모든 여행 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getUserTravels(
            @Parameter(description = "사용자 UUID", required = true)
            @PathVariable Long uuid) {
        try {
            UserDto user = userService.getUserByUid(uuid);
            List<TravelDto> travels = travelService.getTravelsByUserId(user.getId());
            return ResponseUtil.success(travels);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/{uuid}/images")
    @Operation(summary = "사용자의 여행 이미지 목록 조회", description = "특정 사용자가 업로드한 모든 여행 이미지를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getUserImages(
            @Parameter(description = "사용자 UUID", required = true)
            @PathVariable Long uuid) {
        try {
            List<TravelImageDto> images = travelImageService.getTravelImagesByUserId(uuid);
            return ResponseUtil.success(images);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/{uuid}/liked-travels")
    @Operation(summary = "사용자가 좋아요한 여행 목록 조회", description = "특정 사용자가 좋아요한 모든 여행 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getUserLikedTravels(
            @Parameter(description = "사용자 UUID", required = true)
            @PathVariable Long uuid) {
        try {
            List<TravelDto> likedTravels = userLikeService.getLikedTravelsByUserId(uuid);
            return ResponseUtil.success(likedTravels);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/exists/nickname/{nickname}")
    @Operation(summary = "닉네임 중복 체크", description = "해당 닉네임이 이미 존재하는지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<Map<String, Boolean>> existUserNicknameByNickname(
            @Parameter(description = "사용자 닉네임", required = true)
            @PathVariable String nickname) {
        boolean exists = userService.existsByNickname(nickname);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/exists/email/{email}")
    @Operation(summary = "이메일 중복 체크", description = "해당 이메일이 이미 존재하는지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<Map<String, Boolean>> existUserEmailByEmail(
            @Parameter(description = "사용자 이메일", required = true)
            @PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("@userSecurity.isCurrentUser(#uuid) or hasRole('ADMIN')")
    @Operation(summary = "사용자 정보 수정", description = "UUID로 특정 사용자의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
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
    @PreAuthorize("@userSecurity.isCurrentUser(#uuid) or hasRole('ADMIN')")
    @Operation(summary = "사용자 삭제", description = "UUID로 특정 사용자를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
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

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 프로필과 여행 정보 조회", description = "현재 로그인한 사용자의 프로필 정보와 여행 목록을 함께 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getMyProfileWithTravels() {
        try {
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            }

            log.info("사용자 프로필 및 여행 정보 조회 요청 - 사용자 ID: {}", currentUserId);

            // 사용자 정보 조회
            UserDto user = userService.getUserByLoginId(currentUserId);

            // 해당 사용자의 여행 목록 조회
            List<TravelDto> travels = travelService.getTravelsByUserId(currentUserId);

            // 통합 DTO 생성
            UserProfileWithTravelsDto profileWithTravels = UserProfileWithTravelsDto.fromUserAndTravels(
                    userService.getUserEntityByLoginId(currentUserId), travels);

            log.info("사용자 프로필 및 여행 정보 조회 완료 - 사용자: {}, 여행 개수: {}",
                    user.getNickname(), travels.size());

            return ResponseUtil.success(profileWithTravels);
        } catch (IllegalArgumentException e) {
            log.error("사용자 프로필 조회 실패: {}", e.getMessage());
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("사용자 프로필 조회 중 오류 발생", e);
            return ResponseUtil.internalServerError("프로필 조회 중 오류가 발생했습니다.");
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 프로필 정보 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            @Parameter(description = "수정할 프로필 정보", required = true)
            @Valid @RequestBody UserProfileUpdateDto updateDto) {
        try {
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                log.error("인증되지 않은 사용자의 프로필 수정 시도");
                return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            }

            log.info("사용자 프로필 수정 요청 - 사용자 ID: [{}], 수정 정보: [닉네임: {}]",
                    currentUserId, updateDto.getNickname());

            // 프로필 업데이트
            UserDto updatedUser = userService.updateUserProfile(currentUserId, updateDto);

            log.info("사용자 프로필 수정 완료 - 사용자: {}", updatedUser.getNickname());

            return ResponseUtil.success(updatedUser);
        } catch (IllegalArgumentException e) {
            log.error("사용자 프로필 수정 실패: {}", e.getMessage());
            if (e.getMessage().contains("이미 존재하는 닉네임")) {
                return ResponseUtil.badRequest(e.getMessage());
            }
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("사용자 프로필 수정 중 오류 발생", e);
            return ResponseUtil.internalServerError("프로필 수정 중 오류가 발생했습니다.");
        }
    }
}