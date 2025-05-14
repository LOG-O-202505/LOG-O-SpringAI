package com.ssafy.logoserver.domain.area.repository;

import com.ssafy.logoserver.domain.area.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {
    Optional<Area> findByAreaName(String areaName);
}