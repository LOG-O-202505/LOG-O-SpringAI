package com.ssafy.logoserver.domain.travel.repository;

import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TravelRootRepository extends JpaRepository<TravelRoot, Long> {
    List<TravelRoot> findByTravel(Travel travel);
    List<TravelRoot> findByTravelAndDay(Travel travel, Integer day);
    List<TravelRoot> findByTravelDate(LocalDate travelDate);
}