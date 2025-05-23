package com.ssafy.logoserver.domain.travel.entity;

import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.image.entity.TravelImage;
import com.ssafy.logoserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "verifications")
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "place_id", referencedColumnName = "puid"),
            @JoinColumn(name = "place_address", referencedColumnName = "address")
    })
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private double star;

    private String review;

    @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL)
    private List<TravelImage> travelImages = new ArrayList<>();
}
