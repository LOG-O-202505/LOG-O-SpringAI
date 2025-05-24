package com.ssafy.logoserver.domain.area.entity;

import com.ssafy.logoserver.domain.user.entity.UserLike;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 장소 엔티티
 * 여행지나 관심 장소 정보를 저장합니다.
 * UserLike와의 연관관계를 통해 사용자 좋아요 기능을 지원합니다.
 */
@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {

    /**
     * 장소 고유 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long puid;

    /**
     * 장소 주소 (유니크 제약조건)
     */
    @Column(nullable = false, unique = true)
    private String address;

    /**
     * 지역 정보 (다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    /**
     * 장소 이름
     */
    private String name;

    /**
     * 위도
     */
    private Double latitude;

    /**
     * 경도
     */
    private Double longitude;

    /**
     * 이 장소를 좋아요한 사용자 관계 목록
     * UserLike 생성/삭제 시 양방향 관계 유지를 위해 사용
     */
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserLike> userLikes = new ArrayList<>();

    /**
     * UserLike 추가 헬퍼 메서드
     * 양방향 관계 설정을 위해 사용
     * @param userLike 추가할 사용자 좋아요
     */
    public void addUserLike(UserLike userLike) {
        this.userLikes.add(userLike);
    }

    /**
     * UserLike 제거 헬퍼 메서드
     * 양방향 관계 해제를 위해 사용
     * @param userLike 제거할 사용자 좋아요
     */
    public void removeUserLike(UserLike userLike) {
        this.userLikes.remove(userLike);
    }
}