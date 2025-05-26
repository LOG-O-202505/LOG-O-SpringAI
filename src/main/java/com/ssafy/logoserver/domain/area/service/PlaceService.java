package com.ssafy.logoserver.domain.area.service;

import com.ssafy.logoserver.domain.area.dto.PlaceDetailDto;
import com.ssafy.logoserver.domain.area.dto.PlaceDto;
import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.area.repository.PlaceRepository;
import com.ssafy.logoserver.domain.image.entity.TravelImage;
import com.ssafy.logoserver.domain.travel.dto.VerificationDetailDto;
import com.ssafy.logoserver.domain.travel.entity.Verification;
import com.ssafy.logoserver.domain.travel.repository.VerificationRepository;
import com.ssafy.logoserver.domain.user.dto.UserDto;
import com.ssafy.logoserver.domain.user.dto.UserLikeDetailDto;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.entity.UserLike;
import com.ssafy.logoserver.domain.user.repository.UserLikeRepository;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import com.ssafy.logoserver.service.MinIOService;
import com.ssafy.logoserver.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final AreaRepository areaRepository;
    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final UserLikeRepository userLikeRepository;
    private final MinIOService minIOService;

    /**
     * 모든 장소 조회
     */
    public List<PlaceDto> getAllPlaces() {
        return placeRepository.findAll().stream()
                .map(PlaceDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 장소 조회
     */
    public PlaceDto getPlaceById(Long puid, String address) {
        Place place = placeRepository.findById(puid)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 존재하지 않습니다: " + puid + ", " + address));
        return PlaceDto.fromEntity(place);
    }

    /**
     * 지역별 장소 조회
     */
    public List<PlaceDto> getPlacesByAreaId(Long areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다: " + areaId));

        return placeRepository.findByArea(area).stream()
                .map(PlaceDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 이름으로 장소 조회
     */
    public List<PlaceDto> getPlacesByName(String name) {
        return placeRepository.findByName(name).stream()
                .map(PlaceDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 이름 키워드로 장소 검색
     */
    public List<PlaceDto> searchPlacesByNameKeyword(String keyword) {
        return placeRepository.findByNameContaining(keyword).stream()
                .map(PlaceDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 장소 상세 정보 조회 (인증 정보, 사용자 좋아요 정보 포함)
     *
     * @param puid 장소 고유 ID
     * @return 장소 상세 정보 DTO
     */
    @Transactional
    public PlaceDetailDto getPlaceDetailById(Long puid) {
        log.info("장소 상세 정보 조회 시작 - puid: {}", puid);

        // 1. 장소 정보 조회
        Place place = placeRepository.findById(puid)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 존재하지 않습니다: " + puid));

        log.info("장소 조회 완료 - 이름: {}, 주소: {}", place.getName(), place.getAddress());

        // 2. 해당 장소의 모든 인증 정보 조회 (created 기준 최신순 정렬)
        List<Verification> verifications = verificationRepository.findByPlace(place)
                .stream()
                .sorted(Comparator.comparing(Verification::getCreated).reversed()) // 최신순 정렬
                .collect(Collectors.toList());

        log.info("인증 정보 조회 완료 - 총 {}개의 인증", verifications.size());

        // 3. 각 인증에 대해 상세 정보 생성 (작성자 정보 + 이미지 URL)
        List<VerificationDetailDto> verificationDetails = new ArrayList<>();

        for (Verification verification : verifications) {
            try {
                // 작성자 정보 조회
                UserDto userDto = UserDto.fromEntity(verification.getUser());

                // 인증 이미지 URL 생성 (TravelImage를 통해)
                String imageUrl = null;
                TravelImage travelImage = verification.getTravelImages();

                if (travelImage != null && travelImage.getUrl() != null) {
                    try {
                        // MinIO 객체 키로 Presigned URL 생성 (30분 만료)
                        imageUrl = minIOService.generatePresignedUrl(travelImage.getUrl(), 30);
                        log.debug("인증 이미지 URL 생성 완료 - vuid: {}, objectKey: {}",
                                verification.getVuid(), travelImage.getUrl());
                    } catch (Exception e) {
                        log.error("인증 이미지 URL 생성 실패 - vuid: {}", verification.getVuid(), e);
                        // 이미지 URL 생성 실패 시 null로 설정하고 계속 진행
                    }
                }

                // VerificationDetailDto 생성
                VerificationDetailDto verificationDetail = VerificationDetailDto.fromEntity(
                        verification, userDto, imageUrl);

                verificationDetails.add(verificationDetail);

                log.debug("인증 상세 정보 생성 완료 - vuid: {}, 작성자: {}, 이미지 URL: {}",
                        verification.getVuid(), userDto.getNickname(), imageUrl != null ? "있음" : "없음");

            } catch (Exception e) {
                log.error("인증 상세 정보 생성 실패 - vuid: {}", verification.getVuid(), e);
                // 개별 인증 정보 생성 실패 시에도 다른 인증들은 계속 처리
            }
        }

        log.info("인증 상세 정보 생성 완료 - 총 {}개", verificationDetails.size());

        // 4. 현재 로그인 사용자의 좋아요 정보 조회
        String currentUserId = SecurityUtil.getCurrentUserId();
        boolean isLikedByCurrentUser = false;
        UserLikeDetailDto currentUserLike = null;

        if (currentUserId != null) {
            try {
                // 현재 사용자 정보 조회
                Optional<User> currentUserOptional = userRepository.findById(currentUserId);

                if (currentUserOptional.isPresent()) {
                    User currentUser = currentUserOptional.get();

                    // 현재 사용자가 이 장소를 좋아요했는지 확인
                    Optional<UserLike> userLikeOptional = userLikeRepository.findByUserAndPlace(currentUser, place);

                    if (userLikeOptional.isPresent()) {
                        isLikedByCurrentUser = true;
                        currentUserLike = UserLikeDetailDto.fromEntity(userLikeOptional.get());
                        log.info("현재 사용자가 해당 장소를 좋아요함 - 사용자: {}", currentUser.getNickname());
                    } else {
                        log.info("현재 사용자가 해당 장소를 좋아요하지 않음 - 사용자: {}", currentUser.getNickname());
                    }
                }
            } catch (Exception e) {
                log.error("현재 사용자 좋아요 정보 조회 실패", e);
                // 좋아요 정보 조회 실패 시에도 계속 진행
            }
        } else {
            log.info("로그인되지 않은 사용자 - 좋아요 정보 없음");
        }

        // 5. 통계 정보 계산
        // 총 좋아요 수 계산
        int totalLikeCount = userLikeRepository.findByPlace(place).size();

        // 평균 별점 계산
        Double averageRating = null;
        if (!verifications.isEmpty()) {
            double totalRating = verifications.stream()
                    .mapToDouble(Verification::getStar)
                    .sum();
            averageRating = totalRating / verifications.size();
        }

        // 총 리뷰 수 (인증 수와 동일)
        int totalReviewCount = verifications.size();

        log.info("통계 정보 계산 완료 - 좋아요: {}개, 평균별점: {}, 리뷰: {}개",
                totalLikeCount, averageRating, totalReviewCount);

        // 6. PlaceDetailDto 생성 및 반환
        PlaceDetailDto placeDetail = PlaceDetailDto.fromEntity(
                place,
                verificationDetails,
                isLikedByCurrentUser,
                currentUserLike,
                totalLikeCount,
                averageRating,
                totalReviewCount
        );

        log.info("장소 상세 정보 조회 완료 - puid: {}, 인증: {}개, 좋아요: {}개",
                puid, verificationDetails.size(), totalLikeCount);

        return placeDetail;
    }
}