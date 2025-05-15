package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.travel.dto.TravelDto;
import com.ssafy.logoserver.domain.travel.service.TravelService;
import com.ssafy.logoserver.domain.user.service.UserService;
import com.ssafy.logoserver.utils.ResponseUtil;
import com.ssafy.logoserver.utils.SecurityUtil;
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
@RequestMapping("/api/travels")
@RequiredArgsConstructor
@Tag(name = "Travel API", description = "여행 관리 API")
public class TravelController {

    private final TravelService travelService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "모든 여행 조회", description = "시스템에 등록된 모든 여행 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getAllTravels() {
        List<TravelDto> travels = travelService.getAllTravels();
        return ResponseUtil.success(travels);
    }

    @GetMapping("/{tuid}")
    @Operation(summary = "여행 상세 조회", description = "ID로 특정 여행의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelById(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tuid) {
        try {
            TravelDto travel = travelService.getTravelById(tuid);
            return ResponseUtil.success(travel);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자별 여행 조회", description = "특정 사용자의 모든 여행 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelsByUserId(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable String userId) {
        try {
            List<TravelDto> travels = travelService.getTravelsByUserId(userId);
            return ResponseUtil.success(travels);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        }
    }

    @GetMapping("/location/{location}")
    @Operation(summary = "위치별 여행 조회", description = "특정 위치의 모든 여행 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<Map<String, Object>> getTravelsByLocation(
            @Parameter(description = "여행 위치", required = true)
            @PathVariable String location) {
        List<TravelDto> travels = travelService.getTravelsByLocation(location);
        return ResponseUtil.success(travels);
    }

    @PostMapping
    @Operation(summary = "여행 등록", description = "새로운 여행을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> createTravel(
            @Parameter(description = "여행 정보", required = true)
            @RequestBody TravelDto travelDto) {
        try {
            // 현재 로그인한 사용자의 아이디를 가져옴 (로그인 구현 전이라 잠시 주석 처리)
//            String currentUserId = SecurityUtil.getCurrentUserId();

            String currentUserId = userService.getUserById(travelDto.getUserId()).getId();
            if (currentUserId == null) {
                return ResponseUtil.badRequest("로그인이 필요합니다.");
            }

            TravelDto createdTravel = travelService.createTravel(travelDto, currentUserId);
            return ResponseUtil.success(createdTravel);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("여행 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PutMapping("/{tuid}")
    @Operation(summary = "여행 정보 수정", description = "ID로 특정 여행의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> updateTravel(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tuid,
            @Parameter(description = "수정할 여행 정보", required = true)
            @RequestBody TravelDto travelDto) {
        try {
            // 현재 로그인한 사용자의 아이디를 가져옴 (로그인 구현 전이라 잠시 주석 처리)
//            String currentUserId = SecurityUtil.getCurrentUserId();

            String currentUserId = userService.getUserById(travelDto.getUserId()).getId();
            if (currentUserId == null) {
                return ResponseUtil.badRequest("로그인이 필요합니다.");
            }

            TravelDto updatedTravel = travelService.updateTravel(tuid, travelDto, currentUserId);
            return ResponseUtil.success(updatedTravel);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("여행 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{tuid}")
    @Operation(summary = "여행 삭제", description = "ID로 특정 여행을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deleteTravel(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tuid) {
        try {
            // 현재 로그인한 사용자의 아이디를 가져옴 (로그인 구현 전이라 잠시 주석 처리)
//            String currentUserId = SecurityUtil.getCurrentUserId();

            String currentUserId = userService.getUserById(travelService.getTravelById(tuid).getUserId()).getId();
            if (currentUserId == null) {
                return ResponseUtil.badRequest("로그인이 필요합니다.");
            }

            travelService.deleteTravel(tuid, currentUserId);
            return ResponseUtil.success();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            return ResponseUtil.internalServerError("여행 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}