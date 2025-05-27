package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.travel.dto.TravelAreaDto;
import com.ssafy.logoserver.domain.travel.dto.TravelAreaRequestDto;
import com.ssafy.logoserver.domain.travel.service.TravelAreaService;
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

import java.util.List;
import java.util.Map;

/**
 * 여행 지역 컨트롤러
 * 여행 지역 관련 REST API를 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api/travel-areas")
@RequiredArgsConstructor
@Tag(name = "Travel Area API", description = "여행 지역 관리 API")
@Slf4j
public class TravelAreaController {

    private final TravelAreaService travelAreaService;

    /**
     * 모든 여행 지역 조회
     * @return 모든 여행 지역 리스트
     */
    @GetMapping
    @Operation(summary = "모든 여행 지역 조회", description = "시스템에 등록된 모든 여행 지역 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getAllTravelAreas() {
        List<TravelAreaDto> travelAreas = travelAreaService.getAllTravelAreas();
        return ResponseUtil.success(travelAreas);
    }

    /**
     * 여행 지역 상세 조회
     * @param tauid 여행 지역 ID
     * @return 여행 지역 상세 정보
     */
    @GetMapping("/{tauid}")
    @Operation(summary = "여행 지역 상세 조회", description = "ID로 특정 여행 지역의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "여행 지역을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelAreaById(
            @Parameter(description = "여행 지역 ID", required = true)
            @PathVariable Long tauid) {
        try {
            TravelAreaDto travelArea = travelAreaService.getTravelAreaById(tauid);
            return ResponseUtil.success(travelArea);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    /**
     * 여행별 지역 조회
     * @param travelId 여행 ID
     * @return 해당 여행의 모든 지역 정보
     */
    @GetMapping("/travel/{travelId}")
    @Operation(summary = "여행별 지역 조회", description = "특정 여행의 모든 지역 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelAreasByTravelId(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long travelId) {
        try {
            List<TravelAreaDto> travelAreas = travelAreaService.getTravelAreasByTravelId(travelId);
            return ResponseUtil.success(travelAreas);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    /**
     * 여행 루트별 지역 조회
     * @param travelRootId 여행 루트 ID
     * @return 해당 여행 루트의 모든 지역 정보
     */
    @GetMapping("/travel-root/{travelRootId}")
    @Operation(summary = "여행 루트별 지역 조회", description = "특정 여행 루트의 모든 지역 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "여행 루트를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelAreasByTravelRootId(
            @Parameter(description = "여행 루트 ID", required = true)
            @PathVariable Long travelRootId) {
        try {
            List<TravelAreaDto> travelAreas = travelAreaService.getTravelAreasByTravelRootId(travelRootId);
            return ResponseUtil.success(travelAreas);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    /**
     * 여행 지역 등록
     * @param travelAreaDto 여행 지역 정보
     * @return 생성된 여행 지역 정보
     */
    @PostMapping
    @Operation(summary = "여행 지역 등록", description = "새로운 여행 지역을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> createTravelArea(
            @Parameter(description = "여행 지역 정보", required = true)
            @RequestBody TravelAreaDto travelAreaDto) {
        try {
            TravelAreaDto createdTravelArea = travelAreaService.createTravelArea(travelAreaDto);
            return ResponseUtil.success(createdTravelArea);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("여행 지역 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 여행 지역 정보 수정
     * @param tauid 여행 지역 ID
     * @param travelAreaDto 수정할 여행 지역 정보
     * @return 수정된 여행 지역 정보
     */
    @PutMapping("/{tauid}")
    @Operation(summary = "여행 지역 정보 수정", description = "ID로 특정 여행 지역의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "여행 지역을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> updateTravelArea(
            @Parameter(description = "여행 지역 ID", required = true)
            @PathVariable Long tauid,
            @Parameter(description = "수정할 여행 지역 정보", required = true)
            @RequestBody TravelAreaDto travelAreaDto) {
        try {
            TravelAreaDto updatedTravelArea = travelAreaService.updateTravelArea(tauid, travelAreaDto);
            return ResponseUtil.success(updatedTravelArea);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("여행 지역 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 여행 지역 추가 (장소 확인 및 생성 포함)
     * @param requestDto 여행 지역 추가 요청 정보
     * @return 생성된 여행 지역 정보
     */
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "여행 지역 추가", description = "여행 지역을 추가하고 필요시 새로운 장소를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> addTravelArea(
            @Parameter(description = "여행 지역 추가 정보", required = true)
            @RequestBody TravelAreaRequestDto requestDto) {
        try {
            log.info("여행 지역 추가 요청 - travel_id: {}, place_id: {} , address: {}",
                    requestDto.getTravel_id(), requestDto.getPlace_id(), requestDto.getAddress());
            TravelAreaDto travelArea = travelAreaService.addTravelAreaWithPlace(requestDto);
            return ResponseUtil.success(travelArea);
        } catch (IllegalArgumentException e) {
            log.error("여행 지역 추가 실패: {}", e.getMessage());
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("여행 지역 추가 중 오류 발생", e);
            return ResponseUtil.internalServerError("여행 지역 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 여행 지역 삭제
     * @param tauid 여행 지역 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{tauid}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "여행 지역 삭제", description = "ID로 특정 여행 지역을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "여행 지역을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deleteTravelArea(
            @Parameter(description = "여행 지역 ID", required = true)
            @PathVariable Long tauid) {
        try {
            log.info("여행 지역 삭제 요청 - tauid: {}", tauid);
            travelAreaService.deleteTravelArea(tauid);
            return ResponseUtil.success();
        } catch (IllegalArgumentException e) {
            log.error("여행 지역 삭제 실패: {}", e.getMessage());
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("여행 지역 삭제 중 오류 발생", e);
            return ResponseUtil.internalServerError("여행 지역 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}