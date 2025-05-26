package com.ssafy.logoserver.domain.travel.entity;

import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.image.entity.TravelImage;
import com.ssafy.logoserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 위치 인증 엔티티
 * 사용자가 특정 장소를 방문했음을 인증하는 정보를 저장
 * 별점, 리뷰, 인증 이미지 등의 정보를 포함
 */
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "verifications")
public class Verification {

    /**
     * 위치 인증 고유 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vuid;

    /**
     * 인증한 장소 정보
     * 삭제된 장소에 대한 인증은 유지하기 위해 nullable = true
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = true)
    private Place place;

    /**
     * 인증을 수행한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 사용자가 매긴 별점 (1.0 ~ 5.0)
     */
    private double star;

    /**
     * 사용자 리뷰 내용
     */
    private String review;

    /**
     * 인증 생성 시간
     * 데이터베이스에 레코드가 생성될 때 자동으로 현재 시간이 설정됨
     */
    @CreationTimestamp
    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime created;

    /**
     * 인증과 연관된 여행 이미지
     * 인증 시 업로드한 이미지 정보
     */
    @OneToOne(mappedBy = "verification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TravelImage travelImages;
}