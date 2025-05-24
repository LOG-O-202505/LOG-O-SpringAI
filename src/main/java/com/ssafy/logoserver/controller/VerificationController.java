package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.travel.dto.VerificationDto;
import com.ssafy.logoserver.domain.travel.dto.VerificationRequestDto;
import com.ssafy.logoserver.domain.travel.service.VerificationService;
import com.ssafy.logoserver.utils.ResponseUtil;
import com.ssafy.logoserver.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/verifications")
@RequiredArgsConstructor
@Tag(name = "Verification API", description = "위치 인증 관리 API")
@Slf4j
public class VerificationController {

    private final VerificationService verificationService;

    @GetMapping
    @Operation(summary = "모든 위치 인증 조회", description = "시스템에 등록된 모든 위치 인증 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getAllVerifications() {
        List<VerificationDto> verifications = verificationService.getAllVerifications();
        return ResponseUtil.success(verifications);
    }

    @GetMapping("/{vuid}")
    @Operation(summary = "위치 인증 상세 조회", description = "ID로 특정 위치 인증의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "위치 인증을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getVerificationById(
            @Parameter(description = "위치 인증 ID", required = true)
            @PathVariable Long vuid) {
        try {
            VerificationDto verification = verificationService.getVerificationById(vuid);
            return ResponseUtil.success(verification);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자별 위치 인증 조회", description = "특정 사용자의 모든 위치 인증 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getVerificationsByUserId(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId) {
        try {
            List<VerificationDto> verifications = verificationService.getVerificationsByUserId(userId);
            return ResponseUtil.success(verifications);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/place")
    @Operation(summary = "장소별 위치 인증 조회", description = "특정 장소의 모든 위치 인증 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getVerificationsByPlace(
            @Parameter(description = "장소 ID", required = true)
            @RequestParam Long placeId,
            @Parameter(description = "장소 주소", required = true)
            @RequestParam String placeAddress) {
        try {
            List<VerificationDto> verifications = verificationService.getVerificationsByPlace(placeId, placeAddress);
            return ResponseUtil.success(verifications);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/place")
    @Operation(summary = "사용자의 특정 장소 인증 조회", description = "특정 사용자의 특정 장소 인증 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 장소를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getVerificationByUserAndPlace(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "장소 ID", required = true)
            @RequestParam Long placeId,
            @Parameter(description = "장소 주소", required = true)
            @RequestParam String placeAddress) {
        try {
            VerificationDto verification = verificationService.getVerificationByUserAndPlace(userId, placeId, placeAddress);
            return ResponseUtil.success(verification);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    /**
     * 장소 방문 인증
     */
    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "장소 방문 인증", description = "특정 장소에 대한 방문 인증을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> verifyPlace(
            @Parameter(description = "방문 인증 정보", required = true)
            @RequestBody VerificationRequestDto requestDto) {
        try {
            log.info("장소 방문 인증 요청 - pid: {}, address: {}", requestDto.getPid(), requestDto.getAddress());
            VerificationDto verification = verificationService.addVerification(requestDto);
            return ResponseUtil.success(verification);
        } catch (IllegalArgumentException e) {
            log.error("방문 인증 실패: {}", e.getMessage());
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("방문 인증 중 오류 발생", e);
            return ResponseUtil.internalServerError("방문 인증 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}