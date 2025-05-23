package com.ssafy.logoserver.domain.travel.service;

import com.ssafy.logoserver.domain.area.dto.AreaDto;
import com.ssafy.logoserver.domain.area.dto.PlaceDto;
import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.area.entity.PlacePK;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.area.repository.PlaceRepository;
import com.ssafy.logoserver.domain.travel.dto.TravelAreaDto;
import com.ssafy.logoserver.domain.travel.dto.TravelRootDetailDto;
import com.ssafy.logoserver.domain.travel.dto.TravelRootDto;
import com.ssafy.logoserver.domain.travel.dto.VerificationDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import com.ssafy.logoserver.domain.travel.repository.TravelAreaRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRootRepository;
import com.ssafy.logoserver.domain.travel.repository.VerificationRepository;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TravelRootService {

    private final TravelRootRepository travelRootRepository;
    private final TravelRepository travelRepository;
    private final TravelAreaRepository travelAreaRepository;
    private final PlaceRepository placeRepository;
    private final VerificationRepository verificationRepository;
    private final AreaRepository areaRepository;
    private final UserRepository userRepository;

    /**
     * 모든 여행 루트 조회
     */
    public List<TravelRootDto> getAllTravelRoots() {
        return travelRootRepository.findAll().stream()
                .map(TravelRootDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행 루트 조회
     */
    public TravelRootDto getTravelRootById(Long truid) {
        TravelRoot travelRoot = travelRootRepository.findById(truid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + truid));
        return TravelRootDto.fromEntity(travelRoot);
    }

    /**
     * 특정 여행의 모든 루트 조회
     */
    public List<TravelRootDto> getTravelRootsByTravelId(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelId));

        return travelRootRepository.findByTravel(travel).stream()
                .map(TravelRootDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행의 특정 일자 루트 조회
     */
    public List<TravelRootDto> getTravelRootsByTravelAndDay(Long travelId, Integer day) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelId));

        return travelRootRepository.findByTravelAndDay(travel, day).stream()
                .map(TravelRootDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 날짜의 여행 루트 조회
     */
    public List<TravelRootDto> getTravelRootsByDate(LocalDate date) {
        return travelRootRepository.findByTravelDate(date).stream()
                .map(TravelRootDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 여행 루트 생성
     */
    @Transactional
    public TravelRootDto createTravelRoot(TravelRootDto travelRootDto) {
        Travel travel = travelRepository.findById(travelRootDto.getTravelId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelRootDto.getTravelId()));

        Area area = areaRepository.findById(travelRootDto.getAreaId())
                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다: " + travelRootDto.getAreaId()));

        TravelRoot travelRoot = travelRootDto.toEntity(travel, area);
        return TravelRootDto.fromEntity(travelRootRepository.save(travelRoot));
    }

    /**
     * 여행 루트 수정
     */
    @Transactional
    public TravelRootDto updateTravelRoot(Long truid, TravelRootDto travelRootDto) {
        TravelRoot travelRoot = travelRootRepository.findById(truid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + truid));

        Travel travel = travelRoot.getTravel();
        Area area = travelRoot.getArea();

        // 지역 변경 요청이 있는 경우
        if (travelRootDto.getAreaId() != null && !travelRootDto.getAreaId().equals(area.getAuid())) {
            area = areaRepository.findById(travelRootDto.getAreaId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다: " + travelRootDto.getAreaId()));
        }

        // 새로운 여행 루트 정보 생성 (불변성 유지)
        TravelRoot updatedTravelRoot = TravelRoot.builder()
                .truid(travelRoot.getTruid())
                .travel(travel)
                .area(area)
                .day(travelRootDto.getDay() != null ? travelRootDto.getDay() : travelRoot.getDay())
                .travelDate(travelRootDto.getTravelDate() != null ? travelRootDto.getTravelDate() : travelRoot.getTravelDate())
                .travelAreas(travelRoot.getTravelAreas())
                .build();

        return TravelRootDto.fromEntity(travelRootRepository.save(updatedTravelRoot));
    }

    /**
     * 여행 루트 삭제
     */
    @Transactional
    public void deleteTravelRoot(Long truid) {
        TravelRoot travelRoot = travelRootRepository.findById(truid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + truid));

        travelRootRepository.delete(travelRoot);
    }

    /**
     * 여행 루트 상세 정보 조회 (연관 데이터 모두 포함)
     */
    @Transactional
    public TravelRootDetailDto getTravelRootDetailById(Long truid) {
        // 여행 루트 조회
        TravelRoot travelRoot = travelRootRepository.findById(truid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + truid));

        // 지역 정보 조회
        AreaDto area = AreaDto.fromEntity(travelRoot.getArea());

        // 여행 지역 목록 조회
        List<TravelAreaDto> travelAreas = travelAreaRepository.findByTravelDay(travelRoot).stream()
                .map(TravelAreaDto::fromEntity)
                .collect(Collectors.toList());

        // 장소 목록 조회
        List<PlaceDto> places = new ArrayList<>();
        // 인증 목록 조회
        List<VerificationDto> verifications = new ArrayList<>();

        // 각 여행 지역에 연결된 장소와 인증 정보 수집
        for (TravelAreaDto travelArea : travelAreas) {
            // 지역 ID로 장소 조회
            placeRepository.findByArea(travelRoot.getArea()).stream()
                    .map(PlaceDto::fromEntity)
                    .forEach(places::add);

            // 사용자 ID와 장소 ID로 인증 정보 조회
            User user = userRepository.findByUuid(travelArea.getUserId())
                    .orElse(null);
            if (user != null) {
                for (PlaceDto place : places) {
                    PlacePK placePK = new PlacePK(place.getPuid(), place.getAddress());
                    Place placeEntity = placeRepository.findById(placePK).orElse(null);
                    if (placeEntity != null) {
                        verificationRepository.findByUserAndPlace(user, placeEntity)
                                .map(VerificationDto::fromEntity)
                                .ifPresent(verifications::add);
                    }
                }
            }
        }

        // 중복 제거
        places = places.stream().distinct().collect(Collectors.toList());
        verifications = verifications.stream().distinct().collect(Collectors.toList());

        // 상세 DTO 생성 및 반환
        return TravelRootDetailDto.fromEntity(travelRoot, area, travelAreas, places, verifications);
    }
}