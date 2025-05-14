package com.ssafy.logoserver.domain.image.repository;

import com.ssafy.logoserver.domain.image.entity.TravelImage;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelImageRepository extends JpaRepository<TravelImage, Long> {
    List<TravelImage> findByUser(User user);
    List<TravelImage> findByTravel(Travel travel);
    List<TravelImage> findByUserAndTravel(User user, Travel travel);
}