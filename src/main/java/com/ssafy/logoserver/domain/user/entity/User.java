package com.ssafy.logoserver.domain.user.entity;

import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
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
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uuid;

    private String id;

    private String password;

    private String name;

    private String email;

    private String gender;

    @Column(nullable = false)
    private String nickname;

    private LocalDate birthday;

    @Column(name = "profile_image")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Role role;

    @Column(name = "notion_page_id")
    private String notionPageId;

    private String provider; // OAuth2 제공자 (google, naver 등)

    @Column(name = "provider_id")
    private String providerId; // OAuth2 제공자에서의 사용자 ID

    @CreationTimestamp
    private LocalDateTime created;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Travel> travels = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TravelArea> travelAreas = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TravelImage> travelImages = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserLike> userLikes = new ArrayList<>();

    public enum Role {
        ADMIN, USER
    }
}