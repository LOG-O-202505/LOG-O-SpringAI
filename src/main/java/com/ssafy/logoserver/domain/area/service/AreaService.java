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
     * 특정 지역 조회 (이름으로)
     */
    public AreaDto getAreaByName(String areaName) {
        Area area = areaRepository.findByAreaName(areaName)
                .orElseThrow(() -> new IllegalArgumentException("해당 이름의 지역이 존재하지 않습니다: " + areaName));
        return AreaDto.fromEntity(area);
    }

    /**
     * 지역 생성
     */
    @Transactional
    public AreaDto createArea(AreaDto areaDto) {
        // 지역 이름 중복 체크
        if (areaRepository.findByAreaName(areaDto.getAreaName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 지역 이름입니다: " + areaDto.getAreaName());
        }

        Area area = Area.builder()
                .areaName(areaDto.getAreaName())
                .build();

        return AreaDto.fromEntity(areaRepository.save(area));
    }

    /**
     * 지역 정보 수정
     */
    @Transactional
    public AreaDto updateArea(Long auid, AreaDto areaDto) {
        Area area = areaRepository.findById(auid)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다: " + auid));

        // 지역 이름 변경 시 중복 체크
        if (!area.getAreaName().equals(areaDto.getAreaName()) &&
                areaRepository.findByAreaName(areaDto.getAreaName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 지역 이름입니다: " + areaDto.getAreaName());
        }

        // 새로운 지역 정보로 업데이트 (불변성 유지)
        Area updatedArea = Area.builder()
                .auid(area.getAuid())
                .areaName(areaDto.getAreaName())
                .travelRoots(area.getTravelRoots())
                .travelAreas(area.getTravelAreas())
                .build();

        return AreaDto.fromEntity(areaRepository.save(updatedArea));
    }

    /**
     * 지역 삭제
     */
    @Transactional
    public void deleteArea(Long auid) {
        Area area = areaRepository.findById(auid)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다: " + auid));

        // 연관된 여행 루트나 여행 지역이 있는지 확인
        if (!area.getTravelRoots().isEmpty() || !area.getTravelAreas().isEmpty()) {
            throw new IllegalArgumentException("이 지역과 연관된 여행 정보가 있어 삭제할 수 없습니다.");
        }

        areaRepository.delete(area);
    }
}