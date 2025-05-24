package com.ssafy.logoserver.domain.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 여행 결제 요청 DTO
 * POST/PUT 요청 시 클라이언트로부터 받는 결제 정보를 담는 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여행 결제 요청 DTO")
public class TravelPaymentRequestDto {

    @NotNull(message = "여행 ID는 필수입니다.")
    @Schema(description = "여행 고유 ID", example = "1", required = true)
    private Long tuid;

    @NotNull(message = "결제 금액은 필수입니다.")
    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    @Schema(description = "결제 금액", example = "50000", required = true)
    private Integer cost;

    @NotNull(message = "결제 내역은 필수입니다.")
    @Schema(description = "결제 장소/내역", example = "제주도 맛집 - 흑돼지구이", required = true)
    private String history;

    @Schema(description = "결제 시간 (선택사항, 미입력시 현재 시간으로 설정)", example = "2025-05-24T14:30:00")
    private LocalDateTime payment_time;

    /**
     * 결제 시간 getter 메서드
     * JSON의 payment_time 필드와 매핑하기 위한 메서드
     * @return 결제 시간 (null인 경우 현재 시간 반환)
     */
    public LocalDateTime getPaymentTime() {
        return payment_time != null ? payment_time : LocalDateTime.now();
    }
}