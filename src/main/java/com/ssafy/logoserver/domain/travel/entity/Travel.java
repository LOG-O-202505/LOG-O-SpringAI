package com.ssafy.logoserver.domain.travel.entity;

import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.image.entity.TravelImage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Travels")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Travel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(length = 100)
    private String title;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private Integer peoples;

    @Column(length = 10)
    private String season;

    @CreationTimestamp
    private LocalDateTime created;

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL)
    private List<TravelRoot> travelRoots = new ArrayList<>();

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL)
    private List<TravelArea> travelAreas = new ArrayList<>();

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL)
    private List<TravelImage> travelImages = new ArrayList<>();
}