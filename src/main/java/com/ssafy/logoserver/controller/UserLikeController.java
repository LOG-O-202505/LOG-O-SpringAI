package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.user.dto.UserLikeDetailDto;
import com.ssafy.logoserver.domain.user.service.UserLikeService;
import com.ssafy.logoserver.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-likes")
@RequiredArgsConstructor
@Tag(name = "User Like API", description = "사용자 좋아요 관리 API")
@Slf4j
public class UserLikeController {

    private final UserLikeService userLikeService;

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
}