package com.ssafy.logoserver.controller;

import com.ssafy.logoserver.domain.travel.dto.TravelPaymentDto;
import com.ssafy.logoserver.domain.travel.service.TravelPaymentService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travel-payments")
@RequiredArgsConstructor
@Tag(name = "Travel Payment API", description = "여행 결제 내역 관리 API")
@Slf4j
public class TravelPaymentController {

    private final TravelPaymentService travelPaymentService;

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
            List<TravelPaymentDto> travelPayments = travelPaymentService.getTravelPaymentsByTravelId(travelId);
            return ResponseUtil.success(travelPayments);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("여행 결제 내역 조회 중 오류 발생", e);
            return ResponseUtil.internalServerError("여행 결제 내역 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

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
            @RequestBody Map<String, Object> paymentInfo) {
        try {
            // 필수 파라미터 확인
            if (!paymentInfo.containsKey("tuid") || !paymentInfo.containsKey("cost") || !paymentInfo.containsKey("history")) {
                return ResponseUtil.badRequest("필수 정보가 누락되었습니다: tuid, cost, history");
            }

            // 파라미터 추출
            Long travelId = Long.valueOf(paymentInfo.get("tuid").toString());
            Integer cost = Integer.valueOf(paymentInfo.get("cost").toString());
            String history = paymentInfo.get("history").toString();

            // 결제 시간 파라미터 처리 (옵션)
            LocalDateTime paymentTime = null;
            if (paymentInfo.containsKey("payment_time") && paymentInfo.get("payment_time") != null) {
                paymentTime = LocalDateTime.parse(paymentInfo.get("payment_time").toString());
            }

            // 결제 내역 추가
            TravelPaymentDto createdPayment = travelPaymentService.addTravelPayment(travelId, cost, history, paymentTime);

            return ResponseUtil.success(createdPayment);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            log.error("결제 내역 추가 중 오류 발생", e);
            return ResponseUtil.internalServerError("결제 내역 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

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
            @RequestBody Map<String, Object> paymentInfo) {
        try {
            // 파라미터 추출 (모두 선택적)
            Integer cost = paymentInfo.containsKey("cost") ? Integer.valueOf(paymentInfo.get("cost").toString()) : null;
            String history = paymentInfo.containsKey("history") ? paymentInfo.get("history").toString() : null;

            // 결제 시간 파라미터 처리 (옵션)
            LocalDateTime paymentTime = null;
            if (paymentInfo.containsKey("payment_time") && paymentInfo.get("payment_time") != null) {
                paymentTime = LocalDateTime.parse(paymentInfo.get("payment_time").toString());
            }

            // 결제 내역 수정
            TravelPaymentDto updatedPayment = travelPaymentService.updateTravelPayment(tpuid, cost, history, paymentTime);

            return ResponseUtil.success(updatedPayment);
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            log.error("결제 내역 수정 중 오류 발생", e);
            return ResponseUtil.internalServerError("결제 내역 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

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
            // 결제 내역 삭제
            travelPaymentService.deleteTravelPayment(tpuid);

            return ResponseUtil.success();
        } catch (IllegalArgumentException e) {
            return ResponseUtil.notFound(e.getMessage());
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseUtil.error(org.springframework.http.HttpStatus.FORBIDDEN, e.getMessage());
            }
            return ResponseUtil.error(org.springframework.http.HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            log.error("결제 내역 삭제 중 오류 발생", e);
            return ResponseUtil.internalServerError("결제 내역 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}