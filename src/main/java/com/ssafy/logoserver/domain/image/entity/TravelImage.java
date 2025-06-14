package com.ssafy.logoserver.domain.image.entity;

import com.ssafy.logoserver.domain.travel.entity.Verification;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TravelImages")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tiuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ✅ nullable = true로 변경 - Travel이 삭제되어도 TravelImage는 남아있을 수 있음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_id", nullable = true)
    private Travel travel;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id", nullable = false)
    private Verification verification;

    @Column(length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String url;
}