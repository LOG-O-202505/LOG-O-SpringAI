package com.ssafy.logoserver.domain.user.dto;

import com.ssafy.logoserver.domain.area.dto.PlaceDto;
import com.ssafy.logoserver.domain.user.entity.UserLike;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 좋아요 상세 DTO")
public class UserLikeDetailDto {

    @Schema(description = "좋아요 ID", example = "1")
    private Long uluid;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "장소 정보")
    private PlaceDto place;

    /**
     * 엔티티에서 DTO 변환
     */
    public static UserLikeDetailDto fromEntity(UserLike userLike) {
        return UserLikeDetailDto.builder()
                .uluid(userLike.getUluid())
                .userId(userLike.getUser().getUuid())
                .place(PlaceDto.fromEntity(userLike.getPlace()))
                .build();
    }
}