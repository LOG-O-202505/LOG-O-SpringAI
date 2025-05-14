package com.ssafy.logoserver.domain.travel.entity;

import com.ssafy.logoserver.domain.area.entity.Area;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TravelRoots")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long truid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_id", nullable = false)
    private Travel travel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(length = 255)
    private String memo;

    private Integer day;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @OneToMany(mappedBy = "travelDay", cascade = CascadeType.ALL)
    private List<TravelArea> travelAreas = new ArrayList<>();
}