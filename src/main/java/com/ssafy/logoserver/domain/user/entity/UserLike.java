package com.ssafy.logoserver.domain.user.entity;

import com.ssafy.logoserver.domain.area.entity.Place;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_likes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uluid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;
}
