package com.ssafy.logoserver.domain.travel.service;

import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.travel.dto.TravelRootDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import com.ssafy.logoserver.domain.travel.repository.TravelRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRootRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TravelRootService {

    private final TravelRootRepository travelRootRepository;
    private final TravelRepository travelRepository;
    private final AreaRepository areaRepository;

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
}