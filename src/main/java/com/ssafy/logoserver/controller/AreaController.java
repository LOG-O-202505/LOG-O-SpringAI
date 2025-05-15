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

    @GetMapping("/name/{areaName}")
    @Operation(summary = "이름으로 지역 조회", description = "이름으로 특정 지역의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "지역을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getAreaByName(
            @Parameter(description = "지역 이름", required = true)
            @PathVariable String areaName) {
        try {
            AreaDto area = areaService.getAreaByName(areaName);
            return ResponseUtil.success(area);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "지역 등록", description = "새로운 지역을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> createArea(
            @Parameter(description = "지역 정보", required = true)
            @RequestBody AreaDto areaDto) {
        try {
            AreaDto createdArea = areaService.createArea(areaDto);
            return ResponseUtil.success(createdArea);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("지역 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PutMapping("/{auid}")
    @Operation(summary = "지역 정보 수정", description = "ID로 특정 지역의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "지역을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> updateArea(
            @Parameter(description = "지역 ID", required = true)
            @PathVariable Long auid,
            @Parameter(description = "수정할 지역 정보", required = true)
            @RequestBody AreaDto areaDto) {
        try {
            AreaDto updatedArea = areaService.updateArea(auid, areaDto);
            return ResponseUtil.success(updatedArea);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("이미 존재하는 지역 이름입니다")) {
                return ResponseUtil.badRequest(e.getMessage());
            }
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("지역 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{auid}")
    @Operation(summary = "지역 삭제", description = "ID로 특정 지역을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "지역을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deleteArea(
            @Parameter(description = "지역 ID", required = true)
            @PathVariable Long auid) {
        try {
            areaService.deleteArea(auid);
            return ResponseUtil.success();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("연관된 여행 정보가 있어 삭제할 수 없습니다")) {
                return ResponseUtil.badRequest(e.getMessage());
            }
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("지역 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}