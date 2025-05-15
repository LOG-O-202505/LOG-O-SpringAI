package com.ssafy.logoserver.domain.travel.service;

import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.travel.dto.TravelAreaDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import com.ssafy.logoserver.domain.travel.repository.TravelAreaRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRootRepository;
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
public class TravelAreaService {

    private final TravelAreaRepository travelAreaRepository;
    private final TravelRepository travelRepository;
    private final TravelRootRepository travelRootRepository;
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;

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

        return travelAreaRepository.findByTravelDayOrderBySeq(travelRoot).stream()
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
                .seq(travelAreaDto.getSeq() != null ? travelAreaDto.getSeq() : travelArea.getSeq())
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
}