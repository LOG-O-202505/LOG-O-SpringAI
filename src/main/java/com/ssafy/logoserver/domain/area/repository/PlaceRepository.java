package com.ssafy.logoserver.domain.area.repository;

import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByArea(Area area);
    List<Place> findByName(String name);
    List<Place> findByNameContaining(String keyword);
}