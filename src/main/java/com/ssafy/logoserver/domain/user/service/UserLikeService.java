package com.ssafy.logoserver.domain.user.service;

import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.travel.dto.TravelDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
import com.ssafy.logoserver.domain.user.dto.UserLikeDetailDto;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.entity.UserLike;
import com.ssafy.logoserver.domain.user.repository.UserLikeRepository;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import com.ssafy.logoserver.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserLikeService {

    private final UserLikeRepository userLikeRepository;
    private final UserRepository userRepository;

    /**
     * 사용자가 좋아요한 여행 목록 조회
     */
    public List<TravelDto> getLikedTravelsByUserId(Long userId) {
        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + userId));

        return userLikeRepository.findByUser(user).stream()
                .map(UserLike::getPlace)
                .filter(Objects::nonNull)
                .map(Place::getArea)
                .filter(Objects::nonNull)
                .flatMap(area -> area.getTravelAreas().stream())
                .map(TravelArea::getTravel)
                .distinct()
                .map(TravelDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 좋아요 추가
     */
    @Transactional
    public void addUserLike(Long userId, Long placeId, String placeAddress) {
        // 사용자와 장소 확인 후 좋아요 추가 로직 구현
        // ...
    }

    /**
     * 사용자 좋아요 삭제
     */
    @Transactional
    public void removeUserLike(Long userId, Long placeId, String placeAddress) {
        // 사용자와 장소 확인 후 좋아요 삭제 로직 구현
        // ...
    }

    /**
     * 현재 로그인한 사용자의 좋아요 목록 조회
     */
    public List<UserLikeDetailDto> getCurrentUserLikes() {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        User user = userRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        return userLikeRepository.findByUser(user).stream()
                .map(UserLikeDetailDto::fromEntity)
                .collect(Collectors.toList());
    }
}