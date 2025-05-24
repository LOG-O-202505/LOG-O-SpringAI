package com.ssafy.logoserver.domain.user.service;

import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.area.repository.PlaceRepository;
import com.ssafy.logoserver.domain.travel.dto.TravelDto;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
import com.ssafy.logoserver.domain.user.dto.UserLikeDetailDto;
import com.ssafy.logoserver.domain.user.dto.UserLikeRequestDto;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.entity.UserLike;
import com.ssafy.logoserver.domain.user.repository.UserLikeRepository;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import com.ssafy.logoserver.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserLikeService {

    private final UserLikeRepository userLikeRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final AreaRepository areaRepository;

    /**
     * 사용자가 좋아요한 여행 목록 조회
     * @param userId 사용자 ID
     * @return 좋아요한 여행 목록
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
     * 현재 로그인한 사용자의 좋아요 목록 조회
     * @return 좋아요 상세 정보 목록
     */
    public List<UserLikeDetailDto> getCurrentUserLikes() {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        return userLikeRepository.findByUser(user).stream()
                .map(UserLikeDetailDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 장소에 대한 좋아요 생성
     * 1. 지역 정보 확인 (region, sig로 Area 조회)
     * 2. 장소 존재 여부 확인 (address와 area로 Place 조회)
     * 3. 장소가 없으면 새로 생성, 있으면 기존 장소 사용
     * 4. 중복 좋아요 확인 후 UserLike 생성
     * 5. 업데이트된 사용자 좋아요 목록 반환
     *
     * @param requestDto 좋아요 생성 요청 데이터
     * @return 업데이트된 사용자 좋아요 목록
     */
    @Transactional
    public List<UserLikeDetailDto> createUserLike(UserLikeRequestDto requestDto) {
        log.info("좋아요 생성 요청 시작 - 주소: {}, 지역코드: {}-{}",
                requestDto.getAddress(), requestDto.getRegion(), requestDto.getSig());

        // 현재 로그인한 사용자 확인
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 1. 지역 정보 확인 (region과 sig로 Area 조회)
        Area area = findOrThrowArea(requestDto.getRegion(), requestDto.getSig());
        log.info("지역 정보 확인 완료 - 지역 ID: {}", area.getAuid());

        // 2. 장소 존재 여부 확인 및 생성
        Place place = findOrCreatePlace(requestDto, area);
        log.info("장소 처리 완료 - 장소 ID: {}, 주소: {}", place.getPuid(), place.getAddress());

        // 3. 중복 좋아요 확인
        if (userLikeRepository.existsByUserAndPlace(user, place)) {
            throw new IllegalArgumentException("이미 좋아요한 장소입니다.");
        }

        // 4. UserLike 생성
        UserLike userLike = UserLike.builder()
                .user(user)
                .place(place)
                .build();

        userLikeRepository.save(userLike);
        log.info("좋아요 생성 완료 - 사용자: {}, 장소: {}", user.getId(), place.getName());

        // 5. 업데이트된 사용자 좋아요 목록 반환
        return getCurrentUserLikes();
    }

    /**
     * UserLike ID로 좋아요 삭제
     * @param uluid UserLike 고유 ID
     * @return 업데이트된 사용자 좋아요 목록
     */
    @Transactional
    public List<UserLikeDetailDto> deleteUserLikeById(Long uluid) {
        log.info("좋아요 삭제 요청 (ID 방식) - uluid: {}", uluid);

        // 현재 로그인한 사용자 확인
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        // UserLike 조회
        UserLike userLike = userLikeRepository.findById(uluid)
                .orElseThrow(() -> new IllegalArgumentException("해당 좋아요 정보가 존재하지 않습니다: " + uluid));

        // 권한 확인 (본인의 좋아요만 삭제 가능)
        if (!userLike.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("좋아요 삭제 권한이 없습니다.");
        }

        // 좋아요 삭제
        userLikeRepository.delete(userLike);
        log.info("좋아요 삭제 완료 (ID 방식) - uluid: {}, 장소: {}",
                uluid, userLike.getPlace().getName());

        // 업데이트된 사용자 좋아요 목록 반환
        return getCurrentUserLikes();
    }

    /**
     * 주소로 좋아요 삭제
     * 1. 주소로 장소 조회
     * 2. 현재 사용자와 해당 장소의 좋아요 관계 조회
     * 3. 좋아요 삭제
     *
     * @param address 장소 주소
     * @return 업데이트된 사용자 좋아요 목록
     */
    @Transactional
    public List<UserLikeDetailDto> deleteUserLikeByAddress(String address) {
        log.info("좋아요 삭제 요청 (주소 방식) - 주소: {}", address);

        // 현재 로그인한 사용자 확인
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        // 주소로 장소 조회
        List<Place> places = placeRepository.findAll();
        Place targetPlace = places.stream()
                .filter(place -> place.getAddress().equals(address))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 주소의 장소가 존재하지 않습니다: " + address));

        log.info("주소로 장소 조회 완료 - 장소 ID: {}, 이름: {}", targetPlace.getPuid(), targetPlace.getName());

        // 사용자와 장소의 좋아요 관계 조회
        UserLike userLike = userLikeRepository.findByUserAndPlace(user, targetPlace)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소에 대한 좋아요가 존재하지 않습니다."));

        // 좋아요 삭제
        userLikeRepository.delete(userLike);
        log.info("좋아요 삭제 완료 (주소 방식) - 주소: {}, 장소: {}", address, targetPlace.getName());

        // 업데이트된 사용자 좋아요 목록 반환
        return getCurrentUserLikes();
    }

    /**
     * 지역 정보 조회 (region과 sig로 Area 찾기)
     * @param region 시/도 코드
     * @param sig 시/군/구 코드
     * @return 지역 엔티티
     */
    private Area findOrThrowArea(Long region, Long sig) {
        return areaRepository.findByRegionAndSig(region, sig)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("해당 지역이 존재하지 않습니다: region=%d, sig=%d", region, sig)));
    }

    /**
     * 장소 조회 또는 생성
     * 1. address와 area로 기존 장소 조회
     * 2. 없으면 새로운 장소 생성
     *
     * @param requestDto 좋아요 요청 데이터
     * @param area 지역 엔티티
     * @return 장소 엔티티
     */
    private Place findOrCreatePlace(UserLikeRequestDto requestDto, Area area) {
        log.info("장소 조회 시작 - 주소: {}", requestDto.getAddress());

        // address와 area로 기존 장소 조회
        List<Place> existingPlaces = placeRepository.findByArea(area);
        Optional<Place> existingPlace = existingPlaces.stream()
                .filter(place -> place.getAddress().equals(requestDto.getAddress()))
                .findFirst();

        if (existingPlace.isPresent()) {
            log.info("기존 장소 발견 - ID: {}, 이름: {}",
                    existingPlace.get().getPuid(), existingPlace.get().getName());
            return existingPlace.get();
        }

        // 새로운 장소 생성
        log.info("새로운 장소 생성 시작 - 이름: {}", requestDto.getName());

        Place newPlace = Place.builder()
                .address(requestDto.getAddress())
                .area(area)
                .name(requestDto.getName())
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .userLikes(new ArrayList<>()) // 빈 리스트로 초기화
                .build();

        Place savedPlace = placeRepository.save(newPlace);
        log.info("새로운 장소 생성 완료 - ID: {}, 주소: {}", savedPlace.getPuid(), savedPlace.getAddress());

        return savedPlace;
    }
}