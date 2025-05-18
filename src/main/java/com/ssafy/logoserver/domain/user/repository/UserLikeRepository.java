package com.ssafy.logoserver.domain.user.repository;

import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {
    List<UserLike> findByUser(User user);
    List<UserLike> findByPlace(Place place);
    Optional<UserLike> findByUserAndPlace(User user, Place place);
    boolean existsByUserAndPlace(User user, Place place);
}