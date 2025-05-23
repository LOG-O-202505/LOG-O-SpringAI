package com.ssafy.logoserver.domain.area.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
//Place 테이블의 복합키용 클래스
public class PlacePK implements Serializable {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long puid;
    private String address;
}
