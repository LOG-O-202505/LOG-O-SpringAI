package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.travel.dto.TravelPaymentDto;
import com.ssafy.logoserver.domain.travel.dto.TravelPaymentRequestDto;
import com.ssafy.logoserver.domain.travel.dto.TravelPaymentUpdateDto;
import com.ssafy.logoserver.domain.travel.service.TravelPaymentService;
import com.ssafy.logoserver.utils.ResponseUtil;
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

/**
 * 여행 결제 내역 관리 컨트롤러
 * 여행과 관련된 결제 정보를 생성, 조회, 수정, 삭제하는 API를 제공
 */
@RestController
@RequestMapping("/api/travel-payments")
@RequiredArgsConstructor
@Tag(name = "Travel Payment API", description = "여행 결제 내역 관리 API")
@Slf4j
public class TravelPaymentController {

    private final TravelPaymentService travelPaymentService;

    /**
     * 특정 여행의 모든 결제 내역 조회
     * @param travelId 여행 ID
     * @return 결제 내역 목록
     */
    @GetMapping("/travel/{travelId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "여행별 결제 내역 조회", description = "특정 여행의 모든 결제 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getTravelPaymentsByTravelId(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long travelId) {
        try {
            log.info("여행 결제 내역 조회 요청 - 여행 ID: {}", travelId);

            List<TravelPaymentDto> travelPayments = travelPaymentService.getTravelPaymentsByTravelId(travelId);

            log.info("여행 결제 내역 조회 완료 - 여행 ID: {}, 결제 내역 수: {}", travelId, travelPayments.size());
            return ResponseUtil.success(travelPayments);
        } catch (IllegalArgumentException e) {
            log.error("여행 결제 내역 조회 실패 - 여행 ID: {}, 오류: {}", travelId, e.getMessage());
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("여행 결제 내역 조회 중 오류 발생 - 여행 ID: {}", travelId, e);
            return ResponseUtil.internalServerError("여행 결제 내역 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 새로운 결제 내역 추가
     * @param requestDto 결제 내역 정보가 담긴 DTO
     * @return 생성된 결제 내역 정보
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결제 내역 추가", description = "새로운 결제 내역을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> addTravelPayment(
            @Parameter(description = "결제 내역 정보", required = true)
            @Valid @RequestBody TravelPaymentRequestDto requestDto) {
        try {
            log.info("결제 내역 추가 요청 - 여행 ID: {}, 금액: {}, 장소: {}",
                    requestDto.getTuid(), requestDto.getCost(), requestDto.getHistory());

            // 서비스 레이어에 DTO 정보 전달하여 결제 내역 추가
            TravelPaymentDto createdPayment = travelPaymentService.addTravelPayment(
                    requestDto.getTuid(),
                    requestDto.getCost(),
                    requestDto.getHistory(),
                    requestDto.getPaymentTime()
            );

            log.info("결제 내역 추가 완료 - 결제 ID: {}, 여행 ID: {}",
                    createdPayment.getTpuid(), requestDto.getTuid());

            return ResponseUtil.success(createdPayment);
        } catch (IllegalArgumentException e) {
            log.error("결제 내역 추가 실패 - 여행 ID: {}, 오류: {}", requestDto.getTuid(), e.getMessage());
            return ResponseUtil.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("결제 내역 추가 권한 오류 - 여행 ID: {}, 오류: {}", requestDto.getTuid(), e.getMessage());
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            log.error("결제 내역 추가 중 오류 발생 - 여행 ID: {}", requestDto.getTuid(), e);
            return ResponseUtil.internalServerError("결제 내역 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 기존 결제 내역 수정
     * @param tpuid 결제 내역 ID
     * @param updateDto 수정할 결제 내역 정보가 담긴 DTO
     * @return 수정된 결제 내역 정보
     */
    @PutMapping("/{tpuid}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결제 내역 수정", description = "ID로 특정 결제 내역을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "결제 내역을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> updateTravelPayment(
            @Parameter(description = "결제 내역 ID", required = true)
            @PathVariable Long tpuid,
            @Parameter(description = "수정할 결제 내역 정보", required = true)
            @Valid @RequestBody TravelPaymentUpdateDto updateDto) {
        try {
            log.info("결제 내역 수정 요청 - 결제 ID: {}, 수정 정보: [금액: {}, 장소: {}]",
                    tpuid, updateDto.getCost(), updateDto.getHistory());

            // 서비스 레이어에 DTO 정보 전달하여 결제 내역 수정
            TravelPaymentDto updatedPayment = travelPaymentService.updateTravelPayment(
                    tpuid,
                    updateDto.getCost(),
                    updateDto.getHistory(),
                    updateDto.getPaymentTime()
            );

            log.info("결제 내역 수정 완료 - 결제 ID: {}", tpuid);

            return ResponseUtil.success(updatedPayment);
        } catch (IllegalArgumentException e) {
            log.error("결제 내역 수정 실패 - 결제 ID: {}, 오류: {}", tpuid, e.getMessage());
            return ResponseUtil.notFound(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("결제 내역 수정 권한 오류 - 결제 ID: {}, 오류: {}", tpuid, e.getMessage());
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            log.error("결제 내역 수정 중 오류 발생 - 결제 ID: {}", tpuid, e);
            return ResponseUtil.internalServerError("결제 내역 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 결제 내역 삭제
     * @param tpuid 결제 내역 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{tpuid}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "결제 내역 삭제", description = "ID로 특정 결제 내역을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "결제 내역을 찾을 수 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> deleteTravelPayment(
            @Parameter(description = "결제 내역 ID", required = true)
            @PathVariable Long tpuid) {
        try {
            log.info("결제 내역 삭제 요청 - 결제 ID: {}", tpuid);

            // 결제 내역 삭제
            travelPaymentService.deleteTravelPayment(tpuid);

            log.info("결제 내역 삭제 완료 - 결제 ID: {}", tpuid);

            return ResponseUtil.success();
        } catch (IllegalArgumentException e) {
            log.error("결제 내역 삭제 실패 - 결제 ID: {}, 오류: {}", tpuid, e.getMessage());
            return ResponseUtil.notFound(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("결제 내역 삭제 권한 오류 - 결제 ID: {}, 오류: {}", tpuid, e.getMessage());
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            log.error("결제 내역 삭제 중 오류 발생 - 결제 ID: {}", tpuid, e);
            return ResponseUtil.internalServerError("결제 내역 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}