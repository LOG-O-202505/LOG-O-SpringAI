package com.ssafy.logoserver.domain.area.entity;

import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Areas")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auid;

    private String region;

    private String sig;

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL)
    private List<TravelRoot> travelRoots = new ArrayList<>();

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL)
    private List<TravelArea> travelAreas = new ArrayList<>();

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL)
    private List<Place> places = new ArrayList<>();
}