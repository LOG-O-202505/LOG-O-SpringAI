package com.ssafy.logoserver.domain.area.entity;

import com.ssafy.logoserver.domain.user.entity.UserLike;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long puid;

    @Column(nullable = false, unique = true)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    private String name;
    private Double latitude;
    private Double longitude;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL)
    private List<UserLike> userLikes;

}
