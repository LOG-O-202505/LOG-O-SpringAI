package com.ssafy.logoserver.domain.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 여행 결제 수정 요청 DTO
 * PUT 요청 시 클라이언트로부터 받는 결제 정보 수정 데이터를 담는 DTO
 * 모든 필드가 선택사항이며, null이 아닌 필드만 업데이트됨
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여행 결제 수정 요청 DTO")
public class TravelPaymentUpdateDto {

    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    @Schema(description = "수정할 결제 금액 (선택사항)", example = "75000")
    private Integer cost;

    @Schema(description = "수정할 결제 장소/내역 (선택사항)", example = "제주도 카페 - 아메리카노")
    private String history;

    @Schema(description = "수정할 결제 시간 (선택사항)", example = "2025-05-24T16:45:00")
    private LocalDateTime payment_time;

    /**
     * 결제 시간 getter 메서드
     * JSON의 payment_time 필드와 매핑하기 위한 메서드
     * @return 결제 시간
     */
    public LocalDateTime getPaymentTime() {
        return payment_time;
    }
}