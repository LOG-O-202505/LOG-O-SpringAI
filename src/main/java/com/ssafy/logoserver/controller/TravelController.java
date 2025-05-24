package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.travel.dto.TravelCreateDto;
import com.ssafy.logoserver.domain.travel.dto.TravelDetailDto;
import com.ssafy.logoserver.domain.travel.dto.TravelDto;
import com.ssafy.logoserver.domain.travel.dto.TravelUpdateDto;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travels")
@RequiredArgsConstructor
@Tag(name = "Travel API", description = "여행 관리 API")
@Slf4j
public class TravelController {

    private final TravelService travelService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "모든 여행 조회", description = "시스템에 등록된 모든 여행 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getAllTravels(
            @Parameter(description = "상세 정보 포함 여부", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean details) {
        List<TravelDto> travels;
        if (details) {
            travels = travelService.getAllTravelsWithDetails();
        } else {
            travels = travelService.getAllTravels();
        }
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
            @PathVariable Long tuid,
            @Parameter(description = "상세 정보 포함 여부", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean details) {
        try {
            TravelDto travel;
            if (details) {
                travel = travelService.getTravelByIdWithDetails(tuid);
            } else {
                travel = travelService.getTravelById(tuid);
            }
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

    @GetMapping("/title/{title}")
    @Operation(summary = "제목별 여행 조회", description = "제목에 특정 키워드가 포함된 모든 여행 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<Map<String, Object>> getTravelsByTitle(
            @Parameter(description = "여행 제목 키워드", required = true)
            @PathVariable String title) {
        List<TravelDto> travels = travelService.getTravelsByTitle(title);
        return ResponseUtil.success(travels);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "새로운 여행 생성", description = "새로운 여행 계획을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> createTravel(
            @Parameter(description = "여행 생성 정보", required = true)
            @Valid @RequestBody TravelCreateDto createDto) {
        try {
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            }

            log.info("새로운 여행 생성 요청 - 사용자 ID: {}, 여행 제목: {}",
                    currentUserId, createDto.getTitle());

            // 날짜 유효성 검사
            if (createDto.getStartDate().isAfter(createDto.getEndDate())) {
                return ResponseUtil.badRequest("출발일은 도착일보다 이전이어야 합니다.");
            }

            // 여행 생성
            TravelDto createdTravel = travelService.createTravelFromDto(currentUserId, createDto);

            log.info("새로운 여행 생성 완료 - 여행 ID: {}, 제목: {}",
                    createdTravel.getTuid(), createdTravel.getTitle());

            return ResponseUtil.success(createdTravel);
        } catch (IllegalArgumentException e) {
            log.error("여행 생성 실패: {}", e.getMessage());
            return ResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("여행 생성 중 오류 발생", e);
            return ResponseUtil.internalServerError("여행 생성 중 오류가 발생했습니다.");
        }
    }

    @PutMapping("/{tuid}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "여행 정보 수정", description = "ID로 특정 여행의 기본 정보를 수정합니다. (위치, 제목, 인원수, 메모, 총예산만 수정 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> updateTravel(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tuid,
            @Parameter(description = "수정할 여행 기본 정보", required = true)
            @RequestBody TravelUpdateDto updateDto) {
        try {
            // 현재 로그인한 사용자 ID 조회
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            }

            log.info("여행 정보 수정 요청 - 여행 ID: {}, 사용자 ID: {}, 수정 데이터: [제목: {}, 위치: {}, 인원: {}, 예산: {}]",
                    tuid, currentUserId, updateDto.getTitle(), updateDto.getLocation(),
                    updateDto.getPeoples(), updateDto.getTotalBudget());

            // 여행 정보 수정 (새로운 메서드 사용)
            TravelDto updatedTravel = travelService.updateTravelInfo(tuid, updateDto, currentUserId);

            log.info("여행 정보 수정 완료 - 여행 ID: {}, 제목: [{}]", tuid, updatedTravel.getTitle());

            return ResponseUtil.success(updatedTravel);
        } catch (IllegalArgumentException e) {
            log.error("여행 정보 수정 실패 - 여행 ID: {}, 오류: {}", tuid, e.getMessage());
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("여행 정보 수정 중 오류 발생 - 여행 ID: {}", tuid, e);
            return ResponseUtil.internalServerError("여행 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{tuid}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "여행 삭제", description = "ID로 특정 여행을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deleteTravel(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tuid) {
        try {
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
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

    @GetMapping("/{tuid}/detail")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "여행 상세 정보 조회", description = "여행의 모든 상세 정보와 연관 데이터를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelDetail(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tuid) {
        try {
            TravelDetailDto travelDetail = travelService.getTravelDetailById(tuid);
            return ResponseUtil.success(travelDetail);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("여행 상세 정보 조회 중 오류 발생", e);
            return ResponseUtil.internalServerError("여행 상세 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}