package com.ssafy.logoserver.domain.travel.repository;

import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import com.ssafy.logoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelAreaRepository extends JpaRepository<TravelArea, Long> {
    List<TravelArea> findByUser(User user);
    List<TravelArea> findByTravel(Travel travel);
    List<TravelArea> findByTravelDay(TravelRoot travelRoot);
    List<TravelArea> findByTravelDayOrderBySeq(TravelRoot travelRoot);
}