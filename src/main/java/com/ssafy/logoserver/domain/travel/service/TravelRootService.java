package com.ssafy.logoserver.domain.travel.service;

import com.ssafy.logoserver.domain.area.dto.PlaceDto;
import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.area.repository.PlaceRepository;
import com.ssafy.logoserver.domain.travel.dto.TravelAreaDto;
import com.ssafy.logoserver.domain.travel.dto.TravelRootDetailDto;
import com.ssafy.logoserver.domain.travel.dto.TravelRootDto;
import com.ssafy.logoserver.domain.travel.dto.VerificationDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 여행 루트 서비스
 * 여행 루트 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
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
     * @return 모든 여행 루트 DTO 리스트
     */
    public List<TravelRootDto> getAllTravelRoots() {
        return travelRootRepository.findAll().stream()
                .map(TravelRootDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행 루트 조회
     * @param truid 여행 루트 ID
     * @return 여행 루트 DTO
     */
    public TravelRootDto getTravelRootById(Long truid) {
        TravelRoot travelRoot = travelRootRepository.findById(truid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + truid));
        return TravelRootDto.fromEntity(travelRoot);
    }

    /**
     * 특정 여행의 모든 루트 조회
     * @param travelId 여행 ID
     * @return 여행 루트 DTO 리스트
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
     * @param travelId 여행 ID
     * @param day 여행 일차
     * @return 여행 루트 DTO 리스트
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
     * @param date 조회할 날짜
     * @return 여행 루트 DTO 리스트
     */
    public List<TravelRootDto> getTravelRootsByDate(LocalDate date) {
        return travelRootRepository.findByTravelDate(date).stream()
                .map(TravelRootDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 여행 루트 생성
     * @param travelRootDto 여행 루트 DTO
     * @return 생성된 여행 루트 DTO
     */
    @Transactional
    public TravelRootDto createTravelRoot(TravelRootDto travelRootDto) {
        Travel travel = travelRepository.findById(travelRootDto.getTravelId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelRootDto.getTravelId()));

        TravelRoot travelRoot = travelRootDto.toEntity(travel);
        return TravelRootDto.fromEntity(travelRootRepository.save(travelRoot));
    }

    /**
     * 여행 루트 수정
     * @param truid 여행 루트 ID
     * @param travelRootDto 수정할 여행 루트 정보
     * @return 수정된 여행 루트 DTO
     */
    @Transactional
    public TravelRootDto updateTravelRoot(Long truid, TravelRootDto travelRootDto) {
        TravelRoot travelRoot = travelRootRepository.findById(truid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + truid));

        Travel travel = travelRoot.getTravel();

        // 새로운 여행 루트 정보 생성 (불변성 유지)
        TravelRoot updatedTravelRoot = TravelRoot.builder()
                .truid(travelRoot.getTruid())
                .travel(travel)
                .day(travelRootDto.getDay() != null ? travelRootDto.getDay() : travelRoot.getDay())
                .travelDate(travelRootDto.getTravelDate() != null ? travelRootDto.getTravelDate() : travelRoot.getTravelDate())
                .travelAreas(travelRoot.getTravelAreas())
                .build();

        return TravelRootDto.fromEntity(travelRootRepository.save(updatedTravelRoot));
    }

    /**
     * 여행 루트 삭제
     * @param truid 여행 루트 ID
     */
    @Transactional
    public void deleteTravelRoot(Long truid) {
        TravelRoot travelRoot = travelRootRepository.findById(truid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + truid));

        travelRootRepository.delete(travelRoot);
    }

    /**
     * 여행 루트 상세 정보 조회 (연관 데이터 모두 포함)
     * 이제 각 TravelArea에 연결된 Place 정보도 함께 조회
     * @param truid 여행 루트 ID
     * @return 여행 루트 상세 DTO
     */
    @Transactional
    public TravelRootDetailDto getTravelRootDetailById(Long truid) {
        log.info("여행 루트 상세 정보 조회 시작 - truid: {}", truid);

        // 여행 루트 조회
        TravelRoot travelRoot = travelRootRepository.findById(truid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 루트가 존재하지 않습니다: " + truid));

        // 여행 지역 목록 조회 (이제 각 TravelArea에 Place 정보가 포함됨)
        List<TravelAreaDto> travelAreas = travelAreaRepository.findByTravelDay(travelRoot).stream()
                .map(TravelAreaDto::fromEntity)
                .collect(Collectors.toList());

        log.info("여행 지역 조회 완료 - 지역 수: {}", travelAreas.size());

        // 연결된 장소들 수집 (중복 제거)
        List<Place> places = new ArrayList<>();
        List<VerificationDto> verifications = new ArrayList<>();

        for (TravelArea travelArea : travelAreaRepository.findByTravelDay(travelRoot)) {
            // 장소 정보 수집
            if (travelArea.getPlace() != null) {
                places.add(travelArea.getPlace());

                // 해당 장소의 인증 정보 조회
                User user = travelArea.getUser();
                Place place = travelArea.getPlace();

                verificationRepository.findByUserAndPlace(user, place)
                        .map(VerificationDto::fromEntity)
                        .ifPresent(verifications::add);
            }
        }

        // 장소 DTO 변환
        List<PlaceDto> placeDtos = places.stream()
                .map(PlaceDto::fromEntity)
                .collect(Collectors.toList());

        log.info("장소 및 인증 정보 수집 완료 - 장소 수: {}, 인증 수: {}",
                placeDtos.size(), verifications.size());

        // 상세 DTO 생성 및 반환
        return TravelRootDetailDto.fromEntity(travelRoot, travelAreas, placeDtos, verifications);
    }

    /**
     * 특정 여행의 TravelRoot 일괄 삭제 (여행 수정 시 사용)
     * @param travel 여행 엔티티
     */
    @Transactional
    public void deleteAllTravelRootsByTravel(Travel travel) {
        log.info("여행 루트 일괄 삭제 시작 - 여행 ID: {}", travel.getTuid());

        List<TravelRoot> existingRoots = travelRootRepository.findByTravel(travel);

        if (!existingRoots.isEmpty()) {
            travelRootRepository.deleteAll(existingRoots);
            log.info("여행 루트 일괄 삭제 완료 - {}개 삭제됨", existingRoots.size());
        } else {
            log.info("삭제할 여행 루트가 없습니다.");
        }
    }

    /**
     * 여행 기간별 TravelRoot 유효성 검증
     * @param travel 여행 엔티티
     * @return 유효성 검증 결과
     */
    public boolean validateTravelRootsForTravel(Travel travel) {
        List<TravelRoot> travelRoots = travelRootRepository.findByTravel(travel);

        LocalDate startDate = travel.getStartDate();
        LocalDate endDate = travel.getEndDate();
        long expectedDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        log.info("여행 루트 유효성 검증 - 예상 일수: {}, 실제 루트 수: {}",
                expectedDays, travelRoots.size());

        // 루트 수가 예상 일수와 일치하는지 확인
        if (travelRoots.size() != expectedDays) {
            log.warn("여행 루트 수가 일치하지 않습니다.");
            return false;
        }

        // 각 날짜별 루트가 올바르게 생성되었는지 확인
        for (int dayIndex = 1; dayIndex <= expectedDays; dayIndex++) {
            // 람다에서 사용할 수 있도록 final 변수로 복사
            final int currentDay = dayIndex;
            final LocalDate expectedDate = startDate.plusDays(dayIndex - 1);

            boolean dayExists = travelRoots.stream()
                    .anyMatch(root -> root.getDay().equals(currentDay) &&
                            root.getTravelDate().equals(expectedDate));

            if (!dayExists) {
                log.warn("{}일차 루트가 누락되었습니다. 날짜: {}", currentDay, expectedDate);
                return false;
            }
        }

        log.info("여행 루트 유효성 검증 완료 - 모든 루트가 올바르게 생성됨");
        return true;
    }
}