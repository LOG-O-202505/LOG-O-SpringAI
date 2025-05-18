package com.ssafy.logoserver.domain.travel.repository;

import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.travel.entity.Verification;
import com.ssafy.logoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    List<Verification> findByUser(User user);
    List<Verification> findByPlace(Place place);
    Optional<Verification> findByUserAndPlace(User user, Place place);
}