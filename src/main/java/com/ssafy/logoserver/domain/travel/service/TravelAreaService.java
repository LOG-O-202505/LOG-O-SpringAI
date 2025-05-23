package com.ssafy.logoserver.domain.travel.service;

import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.area.entity.PlacePK;
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
     */
    public List<TravelAreaDto> getAllTravelAreas() {
        return travelAreaRepository.findAll().stream()
                .map(TravelAreaDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행 지역 조회
     */
    public TravelAreaDto getTravelAreaById(Long tauid) {
        TravelArea travelArea = travelAreaRepository.findById(tauid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 지역이 존재하지 않습니다: " + tauid));
        return TravelAreaDto.fromEntity(travelArea);
    }

    /**
     * 특정 여행의 모든 지역 조회
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
     */
    @Transactional
    public TravelAreaDto createTravelArea(TravelAreaDto travelAreaDto) {
        User user = userRepository.findByUuid(travelAreaDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + travelAreaDto.getUserId()));

        Area area = areaRepository.findById(travelAreaDto.getAreaId())
                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다: " + travelAreaDto.getAreaId()));

        Travel travel = travelRepository.findById(travelAreaDto.getTravelId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelAreaDto.getTravelId()));

        TravelRoot travelRoot = travelRootRepository.findById(travelAreaDto.getTravelDayId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + travelAreaDto.getTravelDayId()));

        // 권한 확인
        if (!travel.getUser().getUuid().equals(user.getUuid())) {
            throw new IllegalArgumentException("여행 지역 생성 권한이 없습니다.");
        }

        TravelArea travelArea = travelAreaDto.toEntity(user, area, travel, travelRoot);
        return TravelAreaDto.fromEntity(travelAreaRepository.save(travelArea));
    }

    /**
     * 여행 지역 수정
     */
    @Transactional
    public TravelAreaDto updateTravelArea(Long tauid, TravelAreaDto travelAreaDto) {
        TravelArea travelArea = travelAreaRepository.findById(tauid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 지역이 존재하지 않습니다: " + tauid));

        User user = travelArea.getUser();
        Area area = travelArea.getArea();
        Travel travel = travelArea.getTravel();
        TravelRoot travelRoot = travelArea.getTravelDay();

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

        // 새로운 여행 지역 정보 생성 (불변성 유지)
        TravelArea updatedTravelArea = TravelArea.builder()
                .tauid(travelArea.getTauid())
                .user(user)
                .area(area)
                .travel(travel)
                .travelDay(travelRoot)
                .startTime(travelAreaDto.getStartTime() != null ? travelAreaDto.getStartTime() : travelArea.getStartTime())
                .memo(travelAreaDto.getMemo() != null ? travelAreaDto.getMemo() : travelArea.getMemo())
                .build();

        return TravelAreaDto.fromEntity(travelAreaRepository.save(updatedTravelArea));
    }

    /**
     * 여행 지역 삭제
     */
    @Transactional
    public void deleteTravelArea(Long tauid) {
        TravelArea travelArea = travelAreaRepository.findById(tauid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 지역이 존재하지 않습니다: " + tauid));

        travelAreaRepository.delete(travelArea);
    }

    /**
     * 여행 지역 추가 (주소 기반 장소 확인 후 생성)
     * @param requestDto 여행 지역 추가 요청 DTO
     * @return 생성된 여행 지역 DTO
     */
    @Transactional
    public TravelAreaDto addTravelAreaWithPlace(TravelAreaRequestDto requestDto) {
        log.info("여행 지역 추가 요청 처리 시작 - travel_id: {}, address: {}", requestDto.getTravel_id(), requestDto.getAddress());

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

        // Place 찾기 또는 생성
        Place place = findOrCreatePlace(requestDto);
        log.info("장소 처리 완료 - puid: {}, address: {}", place.getPk().getPuid(), place.getPk().getAddress());

        // 여행 지역 엔티티 생성
        TravelArea travelArea = TravelArea.builder()
                .user(user)
                .area(place.getArea())
                .travel(travel)
                .travelDay(travelRoot)
                .startTime(requestDto.getStart())
                .memo(requestDto.getMemo())
                .build();

        TravelArea savedTravelArea = travelAreaRepository.save(travelArea);
        log.info("여행 지역 추가 완료 - tauid: {}", savedTravelArea.getTauid());

        return TravelAreaDto.fromEntity(savedTravelArea);
    }

    /**
     * 주소로 장소 찾기 또는 생성
     * @param requestDto 여행 지역 요청 DTO
     * @return 찾거나 생성된 장소 엔티티
     */
    private Place findOrCreatePlace(TravelAreaRequestDto requestDto) {
        log.info("주소 기반 장소 찾기 시작 - address: {}", requestDto.getAddress());

        // 주소로 장소 찾기 시도
        List<Place> places = placeRepository.findAll();

        // 같은 주소를 가진 장소 찾기
        for (Place place : places) {
            if (place.getPk().getAddress().equals(requestDto.getAddress())) {
                log.info("기존 장소 발견 - puid: {}, address: {}", place.getPk().getPuid(), place.getPk().getAddress());
                return place;
            }
        }

        log.info("기존 장소 없음, 새 장소 생성 시작");

        // 없으면 새로 생성
        // 지역 정보 조회
        Area area = null;
        if (requestDto.getRegion() != null && requestDto.getSig() != null) {
            area = areaRepository.findById(requestDto.getRegion())
                    .orElseThrow(() -> new IllegalArgumentException("해당 지역 정보가 존재하지 않습니다: " + requestDto.getRegion()));
            log.info("지역 정보 조회 - auid: {}", area.getAuid());
        }

        // 새 장소 생성
        PlacePK placePK = PlacePK.builder()
                .address(requestDto.getAddress())
                .build();

        Place newPlace = Place.builder()
                .pk(placePK)
                .area(area)
                .name(requestDto.getName())
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .build();

        Place savedPlace = placeRepository.save(newPlace);
        log.info("새 장소 생성 완료 - puid: {}, address: {}", savedPlace.getPk().getPuid(), savedPlace.getPk().getAddress());

        return savedPlace;
    }

    /**
     * 임시 장소 ID 생성 로직
     * @return 생성된 장소 ID
     */
    private Long generatePlaceId() {
        // 현재 시간을 밀리초로 반환하여 고유한 ID 생성
        return System.currentTimeMillis();
    }
}