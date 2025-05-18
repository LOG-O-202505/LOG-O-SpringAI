package com.ssafy.logoserver.domain.travel.dto;

import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "여행 DTO")
public class TravelDto {

    @Schema(description = "여행 ID", example = "1")
    private Long tuid;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "여행 위치", example = "제주도", required = true)
    private String location;

    @Schema(description = "여행 제목", example = "제주도 가족 여행")
    private String title;

    @Schema(description = "여행 시작일", example = "2025-05-01")
    private LocalDate startDate;

    @Schema(description = "여행 종료일", example = "2025-05-05")
    private LocalDate endDate;

    @Schema(description = "여행 인원수", example = "4")
    private Integer peoples;

    @Schema(description = "여행 계절", example = "봄")
    private String season;

    @Schema(description = "여행 메모", example = "즐거운 제주도 여행")
    private String memo;

    @Schema(description = "총 예산", example = "1000000")
    private Integer totalBudget;

    @Schema(description = "생성일시")
    private LocalDateTime created;

    @Schema(description = "여행 경로 목록")
    private List<TravelRootDto> travelRoots;

    public static TravelDto fromEntity(Travel travel) {
        return TravelDto.builder()
                .tuid(travel.getTuid())
                .userId(travel.getUser().getUuid())
                .location(travel.getLocation())
                .title(travel.getTitle())
                .startDate(travel.getStartDate())
                .endDate(travel.getEndDate())
                .peoples(travel.getPeoples())
                .season(travel.getSeason())
                .memo(travel.getMemo())
                .totalBudget(travel.getTotalBudget())
                .created(travel.getCreated())
                .build();
    }

    public static TravelDto fromEntityWithRoots(Travel travel) {
        List<TravelRootDto> rootDtos = travel.getTravelRoots().stream()
                .map(TravelRootDto::fromEntity)
                .collect(Collectors.toList());

        return TravelDto.builder()
                .tuid(travel.getTuid())
                .userId(travel.getUser().getUuid())
                .location(travel.getLocation())
                .title(travel.getTitle())
                .startDate(travel.getStartDate())
                .endDate(travel.getEndDate())
                .peoples(travel.getPeoples())
                .season(travel.getSeason())
                .memo(travel.getMemo())
                .totalBudget(travel.getTotalBudget())
                .created(travel.getCreated())
                .travelRoots(rootDtos)
                .build();
    }

    public Travel toEntity(User user) {
        return Travel.builder()
                .user(user)
                .location(location)
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
                .peoples(peoples)
                .season(season)
                .memo(memo)
                .totalBudget(totalBudget)
                .build();
    }
}