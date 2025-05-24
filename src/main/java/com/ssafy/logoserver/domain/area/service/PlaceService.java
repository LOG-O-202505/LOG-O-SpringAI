package com.ssafy.logoserver.domain.area.service;

import com.ssafy.logoserver.domain.area.dto.PlaceDto;
import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.area.repository.PlaceRepository;
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
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final AreaRepository areaRepository;

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
}