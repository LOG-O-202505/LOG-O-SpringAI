package com.ssafy.logoserver.domain.travel.repository;

import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TravelRepository extends JpaRepository<Travel, Long> {
    List<Travel> findByUser(User user);
    List<Travel> findByUserAndStartDateGreaterThanEqual(User user, LocalDate date);
    List<Travel> findByLocation(String location);
    List<Travel> findByTitleContaining(String title);
}