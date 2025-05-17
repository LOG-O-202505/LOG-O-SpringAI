package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.area.dto.AreaDto;
import com.ssafy.logoserver.domain.area.service.AreaService;
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
@RequestMapping("/api/areas")
@RequiredArgsConstructor
@Tag(name = "Area API", description = "지역 관리 API")
public class AreaController {

    private final AreaService areaService;

    @GetMapping
    @Operation(summary = "모든 지역 조회", description = "시스템에 등록된 모든 지역 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getAllAreas() {
        List<AreaDto> areas = areaService.getAllAreas();
        return ResponseUtil.success(areas);
    }

    @GetMapping("/{auid}")
    @Operation(summary = "ID로 지역 조회", description = "ID로 특정 지역의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "지역을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getAreaById(
            @Parameter(description = "지역 ID", required = true)
            @PathVariable Long auid) {
        try {
            AreaDto area = areaService.getAreaById(auid);
            return ResponseUtil.success(area);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/name/{regionCode}")
    @Operation(summary = "지역 코드로 해당 지역 시/군/구 조회", description = "지역 코드로 특정 지역의 시/군/구 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "지역을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getSigsByRegion(
            @Parameter(description = "지역 코드", required = true)
            @PathVariable Long regionCode) {
        try {
            List<AreaDto> areas = areaService.getSigsByRegion(regionCode);
            return ResponseUtil.success(areas);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/name/{sigCode}")
    @Operation(summary = "시/군/구 코드로 해당 지역 코드 조회", description = "시/군/구 코드로 해당 지역 코드를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "지역을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getRegionBySig(
            @Parameter(description = "시/군/구 코드", required = true)
            @PathVariable Long sigCode) {
        try {
            AreaDto area = areaService.getRegionBySig(sigCode);
            return ResponseUtil.success(area);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

}