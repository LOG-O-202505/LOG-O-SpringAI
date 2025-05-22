package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여행 생성 DTO")
public class TravelCreateDto {

    @NotBlank(message = "여행 제목은 필수입니다.")
    @Schema(description = "여행 제목", example = "제주도 가족 여행", required = true)
    private String title;

    @NotBlank(message = "주요 목적지는 필수입니다.")
    @Schema(description = "주요 목적지", example = "제주도", required = true)
    private String location;

    @NotNull(message = "여행 인원은 필수입니다.")
    @Min(value = 1, message = "여행 인원은 최소 1명 이상이어야 합니다.")
    @Schema(description = "총 여행 인원", example = "4", required = true)
    private Integer peoples;

    @Schema(description = "총 예산", example = "1000000")
    private Integer totalBudget;

    @NotNull(message = "출발일은 필수입니다.")
    @Schema(description = "출발일", example = "2025-05-01", required = true)
    private LocalDate startDate;

    @NotNull(message = "도착일은 필수입니다.")
    @Schema(description = "도착일", example = "2025-05-05", required = true)
    private LocalDate endDate;

    @Schema(description = "여행 메모", example = "즐거운 제주도 여행")
    private String memo;

    public Travel toEntity(User user) {
        return Travel.builder()
                .user(user)
                .title(title)
                .location(location)
                .peoples(peoples)
                .totalBudget(totalBudget)
                .startDate(startDate)
                .endDate(endDate)
                .memo(memo)
                .build();
    }
}