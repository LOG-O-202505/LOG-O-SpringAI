package com.ssafy.logoserver.domain.travel.service;

import com.ssafy.logoserver.domain.area.dto.AreaDto;
import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.area.repository.PlaceRepository;
import com.ssafy.logoserver.domain.travel.dto.TravelAreaDto;
import com.ssafy.logoserver.domain.travel.dto.TravelAreaRequestDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import com.ssafy.logoserver.domain.travel.repository.TravelAreaRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRootRepository;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import com.ssafy.logoserver.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 여행 지역 서비스
 * 여행 지역 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TravelAreaService {

    private final TravelAreaRepository travelAreaRepository;
    private final TravelRepository travelRepository;
    private final TravelRootRepository travelRootRepository;
    private final AreaRepository areaRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    /**
     * 모든 여행 지역 조회
     * @return 모든 여행 지역 DTO 리스트
     */
    public List<TravelAreaDto> getAllTravelAreas() {
        return travelAreaRepository.findAll().stream()
                .map(TravelAreaDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행 지역 조회
     * @param tauid 여행 지역 ID
     * @return 여행 지역 DTO
     */
    public TravelAreaDto getTravelAreaById(Long tauid) {
        TravelArea travelArea = travelAreaRepository.findById(tauid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 지역이 존재하지 않습니다: " + tauid));
        return TravelAreaDto.fromEntity(travelArea);
    }

    /**
     * 특정 여행의 모든 지역 조회
     * @param travelId 여행 ID
     * @return 여행 지역 DTO 리스트
     */
    public List<TravelAreaDto> getTravelAreasByTravelId(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelId));

        return travelAreaRepository.findByTravel(travel).stream()
                .map(TravelAreaDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행 루트의 모든 지역 조회
     * @param travelRootId 여행 루트 ID
     * @return 여행 지역 DTO 리스트
     */
    public List<TravelAreaDto> getTravelAreasByTravelRootId(Long travelRootId) {
        TravelRoot travelRoot = travelRootRepository.findById(travelRootId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + travelRootId));

        return travelAreaRepository.findByTravelDay(travelRoot).stream()
                .map(TravelAreaDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 여행 지역 생성
     * @param travelAreaDto 여행 지역 DTO
     * @return 생성된 여행 지역 DTO
     */
    @Transactional
    public TravelAreaDto createTravelArea(TravelAreaDto travelAreaDto) {
        // 사용자 확인
        User user = userRepository.findByUuid(travelAreaDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + travelAreaDto.getUserId()));

        // 지역 확인
        Area area = areaRepository.findById(travelAreaDto.getAreaId())
                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다: " + travelAreaDto.getAreaId()));

        // 여행 확인
        Travel travel = travelRepository.findById(travelAreaDto.getTravelId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelAreaDto.getTravelId()));

        // 여행 루트 확인
        TravelRoot travelRoot = travelRootRepository.findById(travelAreaDto.getTravelDayId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + travelAreaDto.getTravelDayId()));

        // 장소 확인 (선택사항)
        Place place = null;
        if (travelAreaDto.getPlaceId() != null) {
            place = placeRepository.findById(travelAreaDto.getPlaceId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 장소가 존재하지 않습니다: " + travelAreaDto.getPlaceId()));
        }

        // 권한 확인 (여행 작성자만 지역 추가 가능)
        if (!travel.getUser().getUuid().equals(user.getUuid())) {
            throw new IllegalArgumentException("여행 지역 생성 권한이 없습니다.");
        }

        // 여행 지역 엔티티 생성
        TravelArea travelArea = travelAreaDto.toEntity(user, area, travel, travelRoot, place);
        return TravelAreaDto.fromEntity(travelAreaRepository.save(travelArea));
    }

    /**
     * 여행 지역 수정
     * @param tauid 여행 지역 ID
     * @param travelAreaDto 수정할 여행 지역 정보
     * @return 수정된 여행 지역 DTO
     */
    @Transactional
    public TravelAreaDto updateTravelArea(Long tauid, TravelAreaDto travelAreaDto) {
        // 기존 여행 지역 조회
        TravelArea travelArea = travelAreaRepository.findById(tauid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 지역이 존재하지 않습니다: " + tauid));

        User user = travelArea.getUser();
        Area area = travelArea.getArea();
        Travel travel = travelArea.getTravel();
        TravelRoot travelRoot = travelArea.getTravelDay();
        Place place = travelArea.getPlace();

        // 지역 변경 요청이 있는 경우
        if (travelAreaDto.getAreaId() != null && !travelAreaDto.getAreaId().equals(area.getAuid())) {
            area = areaRepository.findById(travelAreaDto.getAreaId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다: " + travelAreaDto.getAreaId()));
        }

        // 여행 일자 변경 요청이 있는 경우
        if (travelAreaDto.getTravelDayId() != null && !travelAreaDto.getTravelDayId().equals(travelRoot.getTruid())) {
            travelRoot = travelRootRepository.findById(travelAreaDto.getTravelDayId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + travelAreaDto.getTravelDayId()));
        }

        // 장소 변경 요청이 있는 경우
        if (travelAreaDto.getPlaceId() != null) {
            if (place == null || !travelAreaDto.getPlaceId().equals(place.getPuid())) {
                place = placeRepository.findById(travelAreaDto.getPlaceId())
                        .orElseThrow(() -> new IllegalArgumentException("해당 장소가 존재하지 않습니다: " + travelAreaDto.getPlaceId()));
            }
        }

        // 새로운 여행 지역 정보 생성 (불변성 유지)
        TravelArea updatedTravelArea = TravelArea.builder()
                .tauid(travelArea.getTauid())
                .user(user)
                .area(area)
                .travel(travel)
                .travelDay(travelRoot)
                .place(place) // 수정된 장소 정보
                .startTime(travelAreaDto.getStartTime() != null ? travelAreaDto.getStartTime() : travelArea.getStartTime())
                .memo(travelAreaDto.getMemo() != null ? travelAreaDto.getMemo() : travelArea.getMemo())
                .build();

        return TravelAreaDto.fromEntity(travelAreaRepository.save(updatedTravelArea));
    }

    /**
     * 여행 지역 삭제
     * @param tauid 여행 지역 ID
     */
    @Transactional
    public void deleteTravelArea(Long tauid) {
        TravelArea travelArea = travelAreaRepository.findById(tauid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 지역이 존재하지 않습니다: " + tauid));

        // 권한 확인 (여행 작성자만 삭제 가능)
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !travelArea.getTravel().getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("여행 지역 삭제 권한이 없습니다.");
        }

        travelAreaRepository.delete(travelArea);
    }

    /**
     * 여행 지역 추가 (주소 기반 장소 확인 후 생성)
     * @param requestDto 여행 지역 추가 요청 DTO
     * @return 생성된 여행 지역 DTO
     */
    @Transactional
    public TravelAreaDto addTravelAreaWithPlace(TravelAreaRequestDto requestDto) {
        log.info("여행 지역 추가 요청 처리 시작 - travel_id: {}, place_id: {}, address: {}",
                requestDto.getTravel_id(), requestDto.getPlace_id(), requestDto.getAddress());

        // 여행과 여행 루트 확인
        Travel travel = travelRepository.findById(requestDto.getTravel_id())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + requestDto.getTravel_id()));

        TravelRoot travelRoot = travelRootRepository.findById(requestDto.getTravel_day_id())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + requestDto.getTravel_day_id()));

        // 현재 로그인한 사용자 확인
        String currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("로그인이 필요합니다."));

        // 권한 확인 (여행 작성자만 추가 가능)
        if (!travel.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("여행 지역 추가 권한이 없습니다.");
        }

        // Area 찾기
        Area area = findArea(requestDto.getRegion(), requestDto.getSig());
        log.info("지역 처리 완료 - auid: {}", area.getAuid());

        // Place 처리 (기존 장소 선택 또는 새 장소 생성)
        Place place = null;
        if (requestDto.getPlace_id() != null) {
            // 기존 장소 선택
            place = placeRepository.findById(requestDto.getPlace_id())
                    .orElseThrow(() -> new IllegalArgumentException("해당 장소가 존재하지 않습니다: " + requestDto.getPlace_id()));
            log.info("기존 장소 선택 완료 - puid: {}, name: {}", place.getPuid(), place.getName());
        } else if (requestDto.getAddress() != null && !requestDto.getAddress().trim().isEmpty()) {
            // 새 장소 생성
            place = findOrCreatePlace(requestDto, area);
            log.info("새 장소 처리 완료 - puid: {}, address: {}", place.getPuid(), place.getAddress());
        }

        // 여행 지역 엔티티 생성
        TravelArea travelArea = TravelArea.builder()
                .user(user)
                .area(area)
                .travel(travel)
                .travelDay(travelRoot)
                .place(place) // 장소 정보 추가
                .startTime(requestDto.getStart())
                .memo(requestDto.getMemo())
                .build();

        TravelArea savedTravelArea = travelAreaRepository.save(travelArea);
        log.info("여행 지역 추가 완료 - tauid: {}, place: {}",
                savedTravelArea.getTauid(), place != null ? place.getName() : "없음");

        return TravelAreaDto.fromEntity(savedTravelArea);
    }

    /**
     * 주소로 장소 찾기 또는 생성
     * @param requestDto 여행 지역 요청 DTO
     * @param area 지역 엔티티
     * @return 찾거나 생성된 장소 엔티티
     */
    private Place findOrCreatePlace(TravelAreaRequestDto requestDto, Area area) {
        log.info("주소 기반 장소 찾기 시작 - address: {}", requestDto.getAddress());

        // 주소로 장소 찾기 시도
        List<Place> places = placeRepository.findAll();

        // 같은 주소를 가진 장소 찾기
        for (Place place : places) {
            if (place.getAddress().equals(requestDto.getAddress())) {
                log.info("기존 장소 발견 - puid: {}, address: {}", place.getPuid(), place.getAddress());
                return place;
            }
        }

        log.info("기존 장소 없음, 새 장소 생성 시작");

        // 없으면 새로 생성
        Place newPlace = Place.builder()
                .address(requestDto.getAddress())
                .area(area)
                .name(requestDto.getName())
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .build();

        Place savedPlace = placeRepository.save(newPlace);
        log.info("새 장소 생성 완료 - puid: {}, address: {}", savedPlace.getPuid(), savedPlace.getAddress());

        return savedPlace;
    }

    /**
     * 지역 정보 조회
     * @param region 시/도 코드
     * @param sig 시/군/구 코드
     * @return 지역 엔티티
     */
    private Area findArea(Long region, Long sig) {
        log.info("지역 정보 조회 - region: {}, sig: {}", region, sig);
        return areaRepository.findByRegionAndSig(region, sig)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다: region=" + region + ", sig=" + sig));
    }
}