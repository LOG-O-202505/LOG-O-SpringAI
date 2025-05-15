package com.ssafy.logoserver.domain.image.service;

import com.ssafy.logoserver.domain.image.dto.TravelImageDto;
import com.ssafy.logoserver.domain.image.entity.TravelImage;
import com.ssafy.logoserver.domain.image.repository.TravelImageRepository;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.repository.TravelRepository;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TravelImageService {

    private final TravelImageRepository travelImageRepository;
    private final UserRepository userRepository;
    private final TravelRepository travelRepository;

    /**
     * 모든 여행 이미지 조회
     */
    public List<TravelImageDto> getAllTravelImages() {
        return travelImageRepository.findAll().stream()
                .map(TravelImageDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행 이미지 조회
     */
    public TravelImageDto getTravelImageById(Long tiuid) {
        TravelImage travelImage = travelImageRepository.findById(tiuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 이미지가 존재하지 않습니다: " + tiuid));
        return TravelImageDto.fromEntity(travelImage);
    }

    /**
     * 특정 사용자의 여행 이미지 조회
     */
    public List<TravelImageDto> getTravelImagesByUserId(Long userId) {
        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + userId));

        return travelImageRepository.findByUser(user).stream()
                .map(TravelImageDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행의 이미지 조회
     */
    public List<TravelImageDto> getTravelImagesByTravelId(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelId));

        return travelImageRepository.findByTravel(travel).stream()
                .map(TravelImageDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 특정 여행의 이미지 조회
     */
    public List<TravelImageDto> getTravelImagesByUserAndTravelId(Long userId, Long travelId) {
        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + userId));

        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelId));

        return travelImageRepository.findByUserAndTravel(user, travel).stream()
                .map(TravelImageDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 여행 이미지 생성
     */
    @Transactional
    public TravelImageDto createTravelImage(TravelImageDto travelImageDto) {
        User user = userRepository.findByUuid(travelImageDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + travelImageDto.getUserId()));

        Travel travel = travelRepository.findById(travelImageDto.getTravelId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelImageDto.getTravelId()));

        // 권한 확인 (여행 작성자만 이미지 추가 가능)
        if (!travel.getUser().getUuid().equals(user.getUuid())) {
            throw new IllegalArgumentException("여행 이미지 생성 권한이 없습니다.");
        }

        TravelImage travelImage = travelImageDto.toEntity(user, travel);
        return TravelImageDto.fromEntity(travelImageRepository.save(travelImage));
    }

    /**
     * 여행 이미지 수정
     */
    @Transactional
    public TravelImageDto updateTravelImage(Long tiuid, TravelImageDto travelImageDto) {
        TravelImage travelImage = travelImageRepository.findById(tiuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 이미지가 존재하지 않습니다: " + tiuid));

        // 권한 확인 (여행 이미지 작성자만 수정 가능)
        if (!travelImage.getUser().getUuid().equals(travelImageDto.getUserId())) {
            throw new IllegalArgumentException("여행 이미지 수정 권한이 없습니다.");
        }

        // 여행 변경이 필요한 경우
        Travel travel = travelImage.getTravel();
        if (travelImageDto.getTravelId() != null && !travelImageDto.getTravelId().equals(travel.getTuid())) {
            travel = travelRepository.findById(travelImageDto.getTravelId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelImageDto.getTravelId()));
        }

        // 새로운 여행 이미지 정보 생성 (불변성 유지)
        TravelImage updatedTravelImage = TravelImage.builder()
                .tiuid(travelImage.getTiuid())
                .user(travelImage.getUser())
                .travel(travel)
                .name(travelImageDto.getName() != null ? travelImageDto.getName() : travelImage.getName())
                .url(travelImageDto.getUrl() != null ? travelImageDto.getUrl() : travelImage.getUrl())
                .build();

        return TravelImageDto.fromEntity(travelImageRepository.save(updatedTravelImage));
    }

    /**
     * 여행 이미지 삭제
     */
    @Transactional
    public void deleteTravelImage(Long tiuid, Long userId) {
        TravelImage travelImage = travelImageRepository.findById(tiuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 이미지가 존재하지 않습니다: " + tiuid));

        // 권한 확인 (여행 이미지 작성자만 삭제 가능)
        if (!travelImage.getUser().getUuid().equals(userId)) {
            throw new IllegalArgumentException("여행 이미지 삭제 권한이 없습니다.");
        }

        travelImageRepository.delete(travelImage);
    }
}