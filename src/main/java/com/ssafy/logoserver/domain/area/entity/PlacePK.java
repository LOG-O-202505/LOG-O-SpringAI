package com.ssafy.logoserver.domain.area.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
//Place 테이블의 복합키용 클래스
public class PlacePK implements Serializable {
    private Long puid;
    private String address;
}
