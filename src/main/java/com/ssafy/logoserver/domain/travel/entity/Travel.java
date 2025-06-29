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

    private String location;

    private String title;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private Integer peoples;

    @CreationTimestamp
    private LocalDateTime created;

    private String memo;

    @Column(name = "total_budget")
    private Integer totalBudget;

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL)
    private List<TravelRoot> travelRoots = new ArrayList<>();

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL)
    private List<TravelArea> travelAreas = new ArrayList<>();

    // ✅ CASCADE 제거 - Travel 삭제 시 TravelImage는 삭제되지 않음
    @OneToMany(mappedBy = "travel", fetch = FetchType.LAZY)
    private List<TravelImage> travelImages = new ArrayList<>();

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL)
    private List<TravelPayment> travelPayments = new ArrayList<>();
}