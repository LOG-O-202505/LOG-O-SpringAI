package com.ssafy.logoserver.domain.area.repository;

import com.ssafy.logoserver.domain.area.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 지역 정보 레포지토리
 * 지역 코드 기반 조회 기능을 제공합니다.
 */
public interface AreaRepository extends JpaRepository<Area, Long> {

    /**
     * 지역 코드로 시/군/구 목록 조회
     * @param regionCode 시/도 코드
     * @return 해당 지역의 시/군/구 목록
     */
    List<Area> findSigsByRegion(Long regionCode);

    /**
     * 시/군/구 코드로 지역 조회
     * @param sigCode 시/군/구 코드
     * @return 해당 지역 정보
     */
    Optional<Area> findRegionBySig(Long sigCode);

    /**
     * 시/도와 시/군/구 코드로 지역 조회
     * UserLike 생성 시 지역 검증을 위해 사용
     * @param region 시/도 코드
     * @param sig 시/군/구 코드
     * @return 해당 지역 정보
     */
    Optional<Area> findByRegionAndSig(Long region, Long sig);
}