package com.ssafy.logoserver.domain.travel.entity;

import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "TravelAreas")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tauid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_id", nullable = false)
    private Travel travel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_day_id", nullable = false)
    private TravelRoot travelDay;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    private String memo;
}