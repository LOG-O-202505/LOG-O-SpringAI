package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.travel.entity.TravelPayment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여행 결제 DTO")
public class TravelPaymentDto {

    @Schema(description = "결제 ID", example = "1")
    private Long tpuid;

    @Schema(description = "여행 ID", example = "1")
    private Long travelId;

    @Schema(description = "결제 내역", example = "숙박비")
    private String history;

    @Schema(description = "비용", example = "100000")
    private Integer cost;

    @Schema(description = "결제 시간")
    private LocalDateTime paymentTime;

    /**
     * 엔티티에서 DTO 변환
     */
    public static TravelPaymentDto fromEntity(TravelPayment travelPayment) {
        return TravelPaymentDto.builder()
                .tpuid(travelPayment.getTpuid())
                .travelId(travelPayment.getTravel().getTuid())
                .history(travelPayment.getHistory())
                .cost(travelPayment.getCost())
                .paymentTime(travelPayment.getPaymentTime())
                .build();
    }
}