package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.service.MinioService;
import com.ssafy.logoserver.utils.ResponseUtil;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 파일 업로드 컨트롤러
 * MinIO를 통한 이미지 및 파일 업로드 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Upload API", description = "파일 업로드 관리 API")
@Slf4j
public class FileUploadController {

    private final MinioService minioService;

    /**
     * 여행 이미지 업로드
     * @param file 업로드할 이미지 파일
     * @return 업로드된 파일의 URL
     */
    @PostMapping("/travel-images")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "여행 이미지 업로드", description = "여행 관련 이미지를 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> uploadTravelImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("여행 이미지 업로드 요청 - 파일명: {}, 크기: {} bytes",
                    file.getOriginalFilename(), file.getSize());

            String fileUrl = minioService.uploadTravelImage(file);

            log.info("여행 이미지 업로드 완료 - URL: {}", fileUrl);

            return ResponseUtil.success(Map.of("url", fileUrl));

        } catch (IllegalArgumentException e) {
            log.error("여행 이미지 업로드 실패 - 잘못된 파일: {}", e.getMessage());
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("여행 이미지 업로드 중 오류 발생", e);
            return ResponseUtil.internalServerError("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 인증 이미지 업로드 (위치 인증용)
     * @param file 업로드할 이미지 파일
     * @return 업로드된 파일의 URL
     */
    @PostMapping("/verification-images")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "인증 이미지 업로드", description = "위치 인증용 이미지를 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> uploadVerificationImage(
            @Parameter(description = "업로드할 인증 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("인증 이미지 업로드 요청 - 파일명: {}, 크기: {} bytes",
                    file.getOriginalFilename(), file.getSize());

            String fileUrl = minioService.uploadVerificationImage(file);

            log.info("인증 이미지 업로드 완료 - URL: {}", fileUrl);

            return ResponseUtil.success(Map.of("url", fileUrl));

        } catch (IllegalArgumentException e) {
            log.error("인증 이미지 업로드 실패 - 잘못된 파일: {}", e.getMessage());
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("인증 이미지 업로드 중 오류 발생", e);
            return ResponseUtil.internalServerError("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 프로필 이미지 업로드
     * @param file 업로드할 이미지 파일
     * @return 업로드된 파일의 URL
     */
    @PostMapping("/profile-images")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "프로필 이미지 업로드", description = "사용자 프로필 이미지를 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @Parameter(description = "업로드할 프로필 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("프로필 이미지 업로드 요청 - 파일명: {}, 크기: {} bytes",
                    file.getOriginalFilename(), file.getSize());

            String fileUrl = minioService.uploadProfileImage(file);

            log.info("프로필 이미지 업로드 완료 - URL: {}", fileUrl);

            return ResponseUtil.success(Map.of("url", fileUrl));

        } catch (IllegalArgumentException e) {
            log.error("프로필 이미지 업로드 실패 - 잘못된 파일: {}", e.getMessage());
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 중 오류 발생", e);
            return ResponseUtil.internalServerError("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 삭제
     * @param fileUrl 삭제할 파일의 URL
     * @return 삭제 결과
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "파일 삭제", description = "업로드된 파일을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deleteFile(
            @Parameter(description = "삭제할 파일의 URL", required = true)
            @RequestParam("url") String fileUrl) {
        try {
            log.info("파일 삭제 요청 - URL: {}", fileUrl);

            minioService.deleteFile(fileUrl);

            log.info("파일 삭제 완료 - URL: {}", fileUrl);

            return ResponseUtil.success();

        } catch (IllegalArgumentException e) {
            log.error("파일 삭제 실패 - 잘못된 URL: {}", e.getMessage());
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생", e);
            return ResponseUtil.internalServerError("파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 다운로드용 임시 URL 생성
     * @param fileUrl 원본 파일 URL
     * @return 다운로드용 임시 URL (7일간 유효)
     */
    @GetMapping("/presigned-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "파일 다운로드 URL 생성", description = "파일 다운로드용 임시 URL을 생성합니다. (7일간 유효)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getPresignedUrl(
            @Parameter(description = "원본 파일 URL", required = true)
            @RequestParam("url") String fileUrl) {
        try {
            log.info("임시 URL 생성 요청 - 파일 URL: {}", fileUrl);

            String presignedUrl = minioService.getPresignedUrl(fileUrl);

            log.info("임시 URL 생성 완료 - 원본: {}, 임시 URL: {}", fileUrl, presignedUrl);

            return ResponseUtil.success(Map.of("presignedUrl", presignedUrl));

        } catch (IllegalArgumentException e) {
            log.error("임시 URL 생성 실패 - 잘못된 URL: {}", e.getMessage());
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("임시 URL 생성 중 오류 발생", e);
            return ResponseUtil.internalServerError("임시 URL 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}