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

    @Schema(description = "여행 메모", example = "즐거운 제주도 여행")
    private String memo;

    @Schema(description = "총 예산", example = "1000000")
    private Integer totalBudget;

    @Schema(description = "생성일시")
    private LocalDateTime created;

    @Schema(description = "여행 경로 목록")
    private List<TravelRootDto> travelRoots;

    /**
     * 가장 최근 여행 이미지 URL (MinIO Presigned URL)
     * 사용자 프로필 페이지에서 여행 썸네일로 사용
     * 이미지가 없는 경우 null 값
     */
    @Schema(description = "가장 최근 여행 이미지 URL", example = "https://minio.example.com/bucket/image.jpg")
    private String latestImageUrl;

    /**
     * 기본 엔티티 변환 메서드 (기존 로직 유지)
     */
    public static TravelDto fromEntity(Travel travel) {
        return TravelDto.builder()
                .tuid(travel.getTuid())
                .userId(travel.getUser().getUuid())
                .location(travel.getLocation())
                .title(travel.getTitle())
                .startDate(travel.getStartDate())
                .endDate(travel.getEndDate())
                .peoples(travel.getPeoples())
                .memo(travel.getMemo())
                .totalBudget(travel.getTotalBudget())
                .created(travel.getCreated())
                .latestImageUrl(null) // 기본값으로 null 설정
                .build();
    }

    /**
     * 최근 이미지 URL을 포함한 엔티티 변환 메서드
     * 사용자 프로필 조회 시 사용
     * @param travel 여행 엔티티
     * @param latestImageUrl 가장 최근 이미지의 MinIO Presigned URL
     * @return 최근 이미지 URL이 포함된 TravelDto
     */
    public static TravelDto fromEntityWithLatestImage(Travel travel, String latestImageUrl) {
        return TravelDto.builder()
                .tuid(travel.getTuid())
                .userId(travel.getUser().getUuid())
                .location(travel.getLocation())
                .title(travel.getTitle())
                .startDate(travel.getStartDate())
                .endDate(travel.getEndDate())
                .peoples(travel.getPeoples())
                .memo(travel.getMemo())
                .totalBudget(travel.getTotalBudget())
                .created(travel.getCreated())
                .latestImageUrl(latestImageUrl) // 최근 이미지 URL 설정
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
                .memo(travel.getMemo())
                .totalBudget(travel.getTotalBudget())
                .created(travel.getCreated())
                .travelRoots(rootDtos)
                .latestImageUrl(null) // 기본값으로 null 설정
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
                .memo(memo)
                .totalBudget(totalBudget)
                .build();
    }
}