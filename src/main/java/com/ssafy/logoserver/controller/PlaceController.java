package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.area.dto.PlaceDetailDto;
import com.ssafy.logoserver.domain.area.dto.PlaceDto;
import com.ssafy.logoserver.domain.area.service.PlaceService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Place API", description = "장소 관리 API")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping
    @Operation(summary = "모든 장소 조회", description = "시스템에 등록된 모든 장소 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getAllPlaces() {
        List<PlaceDto> places = placeService.getAllPlaces();
        return ResponseUtil.success(places);
    }

    @GetMapping("/{puid}")
    @Operation(summary = "장소 상세 조회", description = "ID와 주소로 특정 장소의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getPlaceById(
            @Parameter(description = "장소 ID", required = true)
            @PathVariable Long puid,
            @Parameter(description = "장소 주소", required = true)
            @RequestParam String address) {
        try {
            PlaceDto place = placeService.getPlaceById(puid, address);
            return ResponseUtil.success(place);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/area/{areaId}")
    @Operation(summary = "지역별 장소 조회", description = "특정 지역의 모든 장소 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "지역을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getPlacesByAreaId(
            @Parameter(description = "지역 ID", required = true)
            @PathVariable Long areaId) {
        try {
            List<PlaceDto> places = placeService.getPlacesByAreaId(areaId);
            return ResponseUtil.success(places);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "이름으로 장소 조회", description = "특정 이름의 모든 장소 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<Map<String, Object>> getPlacesByName(
            @Parameter(description = "장소 이름", required = true)
            @PathVariable String name) {
        List<PlaceDto> places = placeService.getPlacesByName(name);
        return ResponseUtil.success(places);
    }

    @GetMapping("/search")
    @Operation(summary = "장소 검색", description = "이름에 특정 키워드가 포함된 모든 장소 정보를 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    public ResponseEntity<Map<String, Object>> searchPlacesByNameKeyword(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword) {
        List<PlaceDto> places = placeService.searchPlacesByNameKeyword(keyword);
        return ResponseUtil.success(places);
    }

    @GetMapping("/{puid}/detail")
    @Operation(summary = "장소 상세 정보 조회", description = "장소의 모든 상세 정보(인증 정보, 사용자 좋아요 정보 포함)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getPlaceDetail(
            @Parameter(description = "장소 ID", required = true)
            @PathVariable Long puid) {
        try {
            log.info("장소 상세 정보 조회 요청 - puid: {}", puid);

            PlaceDetailDto placeDetail = placeService.getPlaceDetailById(puid);

            log.info("장소 상세 정보 조회 완료 - 장소명: {}, 인증 수: {}, 좋아요 수: {}",
                    placeDetail.getName(), placeDetail.getTotalReviewCount(), placeDetail.getTotalLikeCount());

            return ResponseUtil.success(placeDetail);
        } catch (IllegalArgumentException e) {
            log.error("장소 상세 정보 조회 실패 - puid: {}, 오류: {}", puid, e.getMessage());
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("장소 상세 정보 조회 중 오류 발생 - puid: {}", puid, e);
            return ResponseUtil.internalServerError("장소 상세 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}