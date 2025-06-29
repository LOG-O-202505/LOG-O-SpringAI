package com.ssafy.logoserver.domain.area.service;

import com.ssafy.logoserver.domain.area.dto.AreaDto;
import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
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
public class AreaService {

    private final AreaRepository areaRepository;

    /**
     * 모든 지역 조회
     */
    public List<AreaDto> getAllAreas() {
        return areaRepository.findAll().stream()
                .map(AreaDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 지역 조회 (ID로)
     */
    public AreaDto getAreaById(Long auid) {
        Area area = areaRepository.findById(auid)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다: " + auid));
        return AreaDto.fromEntity(area);
    }

    /**
     * 특정 지역 조회 (지역 코드로 모든 시/군/구 코드 조회)
     */
    public List<AreaDto> getSigsByRegion(Long regionCode) {
        List<Area> areas = areaRepository.findSigsByRegion(regionCode);
        if(areas == null) {
            throw new IllegalArgumentException("해당 코드의 지역이 존재하지 않습니다: " + regionCode);
        }
        return areas.stream()
                .map(AreaDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 지역 조회 (시/군/구 명으로 지역명 조회)
     */
    public AreaDto getRegionBySig(Long sigCode) {
        Area area = areaRepository.findRegionBySig(sigCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다: " + sigCode));
        return AreaDto.fromEntity(area);
    }

}