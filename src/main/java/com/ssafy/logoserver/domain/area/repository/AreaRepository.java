package com.ssafy.logoserver.domain.area.repository;

import com.ssafy.logoserver.domain.area.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {
    List<Area> findSigsByRegion(Long regionCode);
    Optional<Area> findRegionBySig(Long sigCode);
    Optional<Area> findByRegionAndSig(Long region, Long sig);
}