package com.ssafy.logoserver.domain.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 여행 정보 수정 요청 DTO
 * PUT 요청 시 클라이언트로부터 받는 여행 정보 수정 데이터를 담는 DTO
 * 모든 필드가 선택사항이며, null이 아닌 필드만 업데이트됨
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여행 정보 수정 요청 DTO")
public class TravelUpdateDto {

    @Schema(description = "수정할 여행 위치", example = "제주도")
    private String location;

    @Schema(description = "수정할 여행 제목", example = "제주도 가족 여행")
    private String title;

    @Schema(description = "수정할 여행 인원수", example = "4")
    private Integer peoples;

    @Schema(description = "수정할 여행 메모", example = "즐거운 제주도 여행")
    private String memo;

    @Schema(description = "수정할 총 예산", example = "1000000")
    private Integer totalBudget;
}