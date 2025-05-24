package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.user.dto.UserLikeDetailDto;
import com.ssafy.logoserver.domain.user.dto.UserLikeRequestDto;
import com.ssafy.logoserver.domain.user.service.UserLikeService;
import com.ssafy.logoserver.utils.ResponseUtil;
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
@RequestMapping("/api/user-likes")
@RequiredArgsConstructor
@Tag(name = "User Like API", description = "사용자 좋아요 관리 API")
@Slf4j
public class UserLikeController {

    private final UserLikeService userLikeService;

    /**
     * 현재 로그인한 사용자의 좋아요 목록 조회
     * @return 좋아요 상세 정보 목록
     */
    @GetMapping("/my-likes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내 좋아요 목록 조회", description = "현재 로그인한 사용자의 모든 좋아요 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getMyLikes() {
        try {
            List<UserLikeDetailDto> userLikes = userLikeService.getCurrentUserLikes();
            return ResponseUtil.success(userLikes);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("사용자 좋아요 목록 조회 중 오류 발생", e);
            return ResponseUtil.internalServerError("사용자 좋아요 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 장소에 대한 좋아요 생성
     * 장소가 존재하지 않으면 새로 생성 후 좋아요 추가
     * @param requestDto 좋아요 생성 요청 데이터
     * @return 업데이트된 사용자 좋아요 목록
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "좋아요 생성", description = "장소에 대한 좋아요를 추가합니다. 장소가 없으면 새로 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 좋아요한 장소", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> createUserLike(
            @Parameter(description = "좋아요 생성 요청 정보", required = true)
            @Valid @RequestBody UserLikeRequestDto requestDto) {
        try {
            log.info("좋아요 생성 요청 - 주소: {}, 장소명: {}", requestDto.getAddress(), requestDto.getName());

            List<UserLikeDetailDto> updatedLikes = userLikeService.createUserLike(requestDto);

            log.info("좋아요 생성 완료 - 총 좋아요 수: {}", updatedLikes.size());
            return ResponseUtil.success(updatedLikes);

        } catch (IllegalArgumentException e) {
            log.error("좋아요 생성 실패: {}", e.getMessage());

            if (e.getMessage().contains("이~~비 좋아요한 장소")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.CONFLICT, e.getMessage());
            }
            return ResponseUtil.badRequest(e.getMessage());

        } catch (Exception e) {
            log.error("좋아요 생성 중 오류 발생", e);
            return ResponseUtil.internalServerError("좋아요 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * UserLike ID로 좋아요 삭제
     * @param uluid UserLike 고유 ID
     * @return 업데이트된 사용자 좋아요 목록
     */
    @DeleteMapping("/{uluid}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "좋아요 삭제 (ID 방식)", description = "UserLike ID로 좋아요를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "좋아요 정보를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deleteUserLikeById(
            @Parameter(description = "UserLike ID", required = true)
            @PathVariable Long uluid) {
        try {
            log.info("좋아요 삭제 요청 (ID 방식) - uluid: {}", uluid);

            List<UserLikeDetailDto> updatedLikes = userLikeService.deleteUserLikeById(uluid);

            log.info("좋아요 삭제 완료 (ID 방식) - 남은 좋아요 수: {}", updatedLikes.size());
            return ResponseUtil.success(updatedLikes);

        } catch (IllegalArgumentException e) {
            log.error("좋아요 삭제 실패 (ID 방식): {}", e.getMessage());

            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            if (e.getMessage().contains("존재하지 않습니다")) {
                return ResponseUtil.notFound(e.getMessage());
            }
            return ResponseUtil.badRequest(e.getMessage());

        } catch (Exception e) {
            log.error("좋아요 삭제 중 오류 발생 (ID 방식)", e);
            return ResponseUtil.internalServerError("좋아요 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 주소로 좋아요 삭제
     * @param address 장소 주소
     * @return 업데이트된 사용자 좋아요 목록
     */
    @DeleteMapping("/by-address")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "좋아요 삭제 (주소 방식)", description = "장소 주소로 좋아요를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "404", description = "장소 또는 좋아요 정보를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deleteUserLikeByAddress(
            @Parameter(description = "장소 주소", required = true)
            @RequestParam String address) {
        try {
            log.info("좋아요 삭제 요청 (주소 방식) - 주소: {}", address);

            if (address == null || address.trim().isEmpty()) {
                return ResponseUtil.badRequest("주소는 필수 입력값입니다.");
            }

            List<UserLikeDetailDto> updatedLikes = userLikeService.deleteUserLikeByAddress(address.trim());

            log.info("좋아요 삭제 완료 (주소 방식) - 남은 좋아요 수: {}", updatedLikes.size());
            return ResponseUtil.success(updatedLikes);

        } catch (IllegalArgumentException e) {
            log.error("좋아요 삭제 실패 (주소 방식): {}", e.getMessage());

            if (e.getMessage().contains("존재하지 않습니다")) {
                return ResponseUtil.notFound(e.getMessage());
            }
            return ResponseUtil.badRequest(e.getMessage());

        } catch (Exception e) {
            log.error("좋아요 삭제 중 오류 발생 (주소 방식)", e);
            return ResponseUtil.internalServerError("좋아요 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}