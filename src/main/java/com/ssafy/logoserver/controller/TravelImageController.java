package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.image.dto.TravelImageDto;
import com.ssafy.logoserver.domain.image.service.TravelImageService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travel-images")
@RequiredArgsConstructor
@Tag(name = "Travel Image API", description = "여행 이미지 관리 API")
public class TravelImageController {

    private final TravelImageService travelImageService;

    @GetMapping
    @Operation(summary = "모든 여행 이미지 조회", description = "시스템에 등록된 모든 여행 이미지 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getAllTravelImages() {
        List<TravelImageDto> travelImages = travelImageService.getAllTravelImages();
        return ResponseUtil.success(travelImages);
    }

    @GetMapping("/{tiuid}")
    @Operation(summary = "여행 이미지 상세 조회", description = "ID로 특정 여행 이미지의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "여행 이미지를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelImageById(
            @Parameter(description = "여행 이미지 ID", required = true)
            @PathVariable Long tiuid) {
        try {
            TravelImageDto travelImage = travelImageService.getTravelImageById(tiuid);
            return ResponseUtil.success(travelImage);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자별 여행 이미지 조회", description = "특정 사용자의 모든 여행 이미지 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelImagesByUserId(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId) {
        try {
            List<TravelImageDto> travelImages = travelImageService.getTravelImagesByUserId(userId);
            return ResponseUtil.success(travelImages);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/travel/{travelId}")
    @Operation(summary = "여행별 이미지 조회", description = "특정 여행의 모든 이미지 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelImagesByTravelId(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long travelId) {
        try {
            List<TravelImageDto> travelImages = travelImageService.getTravelImagesByTravelId(travelId);
            return ResponseUtil.success(travelImages);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/travel/{travelId}")
    @Operation(summary = "사용자의 특정 여행 이미지 조회", description = "특정 사용자의 특정 여행에 대한 이미지 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelImagesByUserAndTravelId(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long travelId) {
        try {
            List<TravelImageDto> travelImages = travelImageService.getTravelImagesByUserAndTravelId(userId, travelId);
            return ResponseUtil.success(travelImages);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    /**
     * 여행 이미지 조회 URL 생성
     * MinIO에 저장된 이미지에 대한 Presigned URL을 생성하여 반환합니다.
     */
    @GetMapping("/{tiuid}/url")
    @Operation(summary = "여행 이미지 조회 URL 생성", description = "여행 이미지에 대한 임시 접근 URL을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL 생성 성공"),
            @ApiResponse(responseCode = "404", description = "여행 이미지를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelImageUrl(
            @Parameter(description = "여행 이미지 ID", required = true)
            @PathVariable Long tiuid,
            @Parameter(description = "URL 만료 시간(분)", example = "30")
            @RequestParam(defaultValue = "30") int expiryMinutes) {
        try {
            String imageUrl = travelImageService.getTravelImageUrl(tiuid, expiryMinutes);

            Map<String, Object> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("expiryMinutes", expiryMinutes);

            return ResponseUtil.success(response);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("이미지 URL 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 여행별 이미지 URL 목록 조회
     * 특정 여행의 모든 이미지에 대한 Presigned URL 목록을 생성하여 반환합니다.
     */
    @GetMapping("/travel/{travelId}/urls")
    @Operation(summary = "여행별 이미지 URL 목록 조회", description = "특정 여행의 모든 이미지 URL을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL 목록 생성 성공"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelImageUrls(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long travelId,
            @Parameter(description = "URL 만료 시간(분)", example = "30")
            @RequestParam(defaultValue = "30") int expiryMinutes) {
        try {
            List<Map<String, Object>> imageUrls = travelImageService.getTravelImageUrls(travelId, expiryMinutes);
            return ResponseUtil.success(imageUrls);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("이미지 URL 목록 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자별 이미지 URL 목록 조회
     * 특정 사용자의 모든 여행 이미지에 대한 Presigned URL 목록을 생성하여 반환합니다.
     */
    @GetMapping("/user/{userId}/urls")
    @Operation(summary = "사용자별 이미지 URL 목록 조회", description = "특정 사용자의 모든 여행 이미지 URL을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL 목록 생성 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getUserImageUrls(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "URL 만료 시간(분)", example = "30")
            @RequestParam(defaultValue = "30") int expiryMinutes) {
        try {
            List<Map<String, Object>> imageUrls = travelImageService.getUserImageUrls(userId, expiryMinutes);
            return ResponseUtil.success(imageUrls);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("사용자 이미지 URL 목록 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "여행 이미지 등록", description = "새로운 여행 이미지를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> createTravelImage(
            @Parameter(description = "여행 이미지 정보", required = true)
            @RequestBody TravelImageDto travelImageDto) {
        try {
            TravelImageDto createdTravelImage = travelImageService.createTravelImage(travelImageDto);
            return ResponseUtil.success(createdTravelImage);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("여행 이미지 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PutMapping("/{tiuid}")
    @Operation(summary = "여행 이미지 정보 수정", description = "ID로 특정 여행 이미지의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "여행 이미지를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> updateTravelImage(
            @Parameter(description = "여행 이미지 ID", required = true)
            @PathVariable Long tiuid,
            @Parameter(description = "수정할 여행 이미지 정보", required = true)
            @RequestBody TravelImageDto travelImageDto) {
        try {
            TravelImageDto updatedTravelImage = travelImageService.updateTravelImage(tiuid, travelImageDto);
            return ResponseUtil.success(updatedTravelImage);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("여행 이미지 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{tiuid}")
    @Operation(summary = "여행 이미지 삭제", description = "ID로 특정 여행 이미지를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "여행 이미지를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deleteTravelImage(
            @Parameter(description = "여행 이미지 ID", required = true)
            @PathVariable Long tiuid,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId) {
        try {
            travelImageService.deleteTravelImage(tiuid, userId);
            return ResponseUtil.success();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("여행 이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}