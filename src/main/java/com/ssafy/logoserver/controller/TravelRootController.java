package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.travel.dto.TravelRootDto;
import com.ssafy.logoserver.domain.travel.service.TravelRootService;
import com.ssafy.logoserver.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travel-roots")
@RequiredArgsConstructor
@Tag(name = "Travel Root API", description = "여행 루트 관리 API")
public class TravelRootController {

    private final TravelRootService travelRootService;

    @GetMapping
    @Operation(summary = "모든 여행 루트 조회", description = "시스템에 등록된 모든 여행 루트 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getAllTravelRoots() {
        List<TravelRootDto> travelRoots = travelRootService.getAllTravelRoots();
        return ResponseUtil.success(travelRoots);
    }

    @GetMapping("/{truid}")
    @Operation(summary = "여행 루트 상세 조회", description = "ID로 특정 여행 루트의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "여행 루트를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelRootById(
            @Parameter(description = "여행 루트 ID", required = true)
            @PathVariable Long truid) {
        try {
            TravelRootDto travelRoot = travelRootService.getTravelRootById(truid);
            return ResponseUtil.success(travelRoot);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/travel/{travelId}")
    @Operation(summary = "여행별 루트 조회", description = "특정 여행의 모든 루트 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelRootsByTravelId(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long travelId) {
        try {
            List<TravelRootDto> travelRoots = travelRootService.getTravelRootsByTravelId(travelId);
            return ResponseUtil.success(travelRoots);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/travel/{travelId}/day/{day}")
    @Operation(summary = "여행 일자별 루트 조회", description = "특정 여행의 특정 일자 루트 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelRootsByTravelAndDay(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long travelId,
            @Parameter(description = "여행 일자", required = true)
            @PathVariable Integer day) {
        try {
            List<TravelRootDto> travelRoots = travelRootService.getTravelRootsByTravelAndDay(travelId, day);
            return ResponseUtil.success(travelRoots);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "날짜별 여행 루트 조회", description = "특정 날짜의 모든 여행 루트 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<Map<String, Object>> getTravelRootsByDate(
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)", required = true)
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TravelRootDto> travelRoots = travelRootService.getTravelRootsByDate(date);
        return ResponseUtil.success(travelRoots);
    }

    @PostMapping
    @Operation(summary = "여행 루트 등록", description = "새로운 여행 루트를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> createTravelRoot(
            @Parameter(description = "여행 루트 정보", required = true)
            @RequestBody TravelRootDto travelRootDto) {
        try {
            TravelRootDto createdTravelRoot = travelRootService.createTravelRoot(travelRootDto);
            return ResponseUtil.success(createdTravelRoot);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("여행 루트 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PutMapping("/{truid}")
    @Operation(summary = "여행 루트 정보 수정", description = "ID로 특정 여행 루트의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "여행 루트를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> updateTravelRoot(
            @Parameter(description = "여행 루트 ID", required = true)
            @PathVariable Long truid,
            @Parameter(description = "수정할 여행 루트 정보", required = true)
            @RequestBody TravelRootDto travelRootDto) {
        try {
            TravelRootDto updatedTravelRoot = travelRootService.updateTravelRoot(truid, travelRootDto);
            return ResponseUtil.success(updatedTravelRoot);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("여행 루트 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{truid}")
    @Operation(summary = "여행 루트 삭제", description = "ID로 특정 여행 루트를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "여행 루트를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deleteTravelRoot(
            @Parameter(description = "여행 루트 ID", required = true)
            @PathVariable Long truid) {
        try {
            travelRootService.deleteTravelRoot(truid);
            return ResponseUtil.success();
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("여행 루트 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}