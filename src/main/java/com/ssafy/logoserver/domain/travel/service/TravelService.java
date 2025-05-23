package com.ssafy.logoserver.domain.travel.service;

import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.image.dto.TravelImageDto;
import com.ssafy.logoserver.domain.image.repository.TravelImageRepository;
import com.ssafy.logoserver.domain.travel.dto.TravelCreateDto;
import com.ssafy.logoserver.domain.travel.dto.TravelDetailDto;
import com.ssafy.logoserver.domain.travel.dto.TravelDto;
import com.ssafy.logoserver.domain.travel.dto.TravelPaymentDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelRoot;
import com.ssafy.logoserver.domain.travel.repository.TravelPaymentRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRootRepository;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TravelService {

    private final TravelRepository travelRepository;
    private final TravelImageRepository travelImageRepository;
    private final TravelPaymentRepository travelPaymentRepository;
    private final TravelRootRepository travelRootRepository;
    private final AreaRepository areaRepository;
    private final UserRepository userRepository;

    /**
     * 모든 여행 목록 조회
     */
    public List<TravelDto> getAllTravels() {
        return travelRepository.findAll().stream()
                .map(TravelDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 모든 여행 목록 조회 (연관 데이터 포함)
     */
    public List<TravelDto> getAllTravelsWithDetails() {
        return travelRepository.findAll().stream()
                .map(TravelDto::fromEntityWithRoots)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행 조회
     */
    public TravelDto getTravelById(Long tuid) {
        Travel travel = travelRepository.findById(tuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + tuid));
        return TravelDto.fromEntity(travel);
    }

    /**
     * 특정 여행 조회 (연관 데이터 포함)
     */
    public TravelDto getTravelByIdWithDetails(Long tuid) {
        Travel travel = travelRepository.findById(tuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + tuid));
        return TravelDto.fromEntityWithRoots(travel);
    }

    /**
     * 특정 사용자의 여행 목록 조회
     */
    public List<TravelDto> getTravelsByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 ID가 존재하지 않습니다: " + userId));

        return travelRepository.findByUser(user).stream()
                .map(TravelDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 위치의 여행 목록 조회
     */
    public List<TravelDto> getTravelsByLocation(String location) {
        return travelRepository.findByLocation(location).stream()
                .map(TravelDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 여행 제목으로 여행 목록 조회
     */
    public List<TravelDto> getTravelsByTitle(String title) {
        return travelRepository.findByTitleContaining(title).stream()
                .map(TravelDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * TravelCreateDto를 사용한 여행 생성 및 기간별 TravelRoot 자동 생성
     * @param userId 사용자 ID
     * @param createDto 여행 생성 정보
     * @return 생성된 여행 DTO
     */
    @Transactional
    public TravelDto createTravelFromDto(String userId, TravelCreateDto createDto) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 ID가 존재하지 않습니다: " + userId));

        log.info("새로운 여행 생성 - 사용자: {}, 제목: {}", user.getNickname(), createDto.getTitle());

        // 여행 엔티티 생성 및 저장
        Travel travel = createDto.toEntity(user);
        Travel savedTravel = travelRepository.save(travel);

        log.info("여행 생성 완료 - ID: {}, 제목: {}", savedTravel.getTuid(), savedTravel.getTitle());

        // 여행 기간에 따른 TravelRoot 자동 생성
        createTravelRootsForTravelPeriod(savedTravel);

        return TravelDto.fromEntity(savedTravel);
    }

    /**
     * 여행 기간에 따른 TravelRoot 자동 생성
     * @param travel 생성된 여행 엔티티
     */
    private void createTravelRootsForTravelPeriod(Travel travel) {
        LocalDate startDate = travel.getStartDate();
        LocalDate endDate = travel.getEndDate();

        log.info("여행 루트 자동 생성 시작 - 여행 ID: {}, 시작일: {}, 종료일: {}",
                travel.getTuid(), startDate, endDate);

        // 여행 기간 계산 (시작일과 종료일 포함)
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        log.info("총 여행 기간: {}일", daysBetween);

        // 기본 지역 정보 조회 (첫 번째 지역을 기본으로 사용)
        // 실제 프로젝트에서는 여행 위치에 따른 적절한 지역을 선택하는 로직이 필요할 수 있음
        Area defaultArea = getDefaultAreaForTravel(travel);

        List<TravelRoot> travelRoots = new ArrayList<>();

        // 각 날짜별로 TravelRoot 생성
        for (int dayIndex = 0; dayIndex < daysBetween; dayIndex++) {
            LocalDate currentDate = startDate.plusDays(dayIndex);
            int dayNumber = dayIndex + 1; // 1일차, 2일차, 3일차...

            log.info("{}일차 여행 루트 생성 - 날짜: {}", dayNumber, currentDate);

            TravelRoot travelRoot = TravelRoot.builder()
                    .travel(travel)
                    .area(defaultArea)
                    .day(dayNumber)
                    .travelDate(currentDate)
                    .travelAreas(new ArrayList<>()) // 빈 리스트로 초기화
                    .build();

            travelRoots.add(travelRoot);
        }

        // 생성된 TravelRoot들을 일괄 저장
        List<TravelRoot> savedTravelRoots = travelRootRepository.saveAll(travelRoots);

        log.info("여행 루트 자동 생성 완료 - 총 {}개의 루트 생성됨", savedTravelRoots.size());

        // 각 생성된 루트의 상세 정보 로깅
        for (TravelRoot savedRoot : savedTravelRoots) {
            log.info("생성된 루트 - ID: {}, {}일차, 날짜: {}",
                    savedRoot.getTruid(), savedRoot.getDay(), savedRoot.getTravelDate());
        }
    }

    /**
     * 여행에 대한 기본 지역 정보 조회
     * 현재는 첫 번째 지역을 반환하지만, 실제로는 여행 위치에 맞는 지역을 찾는 로직이 필요
     * @param travel 여행 엔티티
     * @return 기본 지역 엔티티
     */
    private Area getDefaultAreaForTravel(Travel travel) {
        // 여행 위치(location)를 기반으로 적절한 지역을 찾는 로직
        // 현재는 임시로 첫 번째 지역을 반환
        List<Area> areas = areaRepository.findAll();

        if (areas.isEmpty()) {
            log.warn("등록된 지역이 없습니다. 기본 지역을 생성합니다.");
            // 기본 지역 생성 (실제 프로젝트에서는 적절한 지역 정보를 사용해야 함)
            Area defaultArea = Area.builder()
                    .region(1L) // 기본 시/도 코드
                    .sig(1L)    // 기본 시/군/구 코드
                    .travelRoots(new ArrayList<>())
                    .travelAreas(new ArrayList<>())
                    .places(new ArrayList<>())
                    .build();

            return areaRepository.save(defaultArea);
        }

        // TODO: 여행 위치(travel.getLocation())와 매칭되는 적절한 지역을 찾는 로직 구현
        // 예: 여행 위치가 "제주도"인 경우 제주도에 해당하는 지역 코드를 찾아서 반환

        log.info("기본 지역 사용 - 지역 ID: {}", areas.get(0).getAuid());
        return areas.get(0);
    }

    /**
     * 여행 생성 (기존 메서드 유지)
     */
    @Transactional
    public TravelDto createTravel(TravelDto travelDto, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 ID가 존재하지 않습니다: " + userId));

        Travel travel = travelDto.toEntity(user);
        Travel savedTravel = travelRepository.save(travel);

        // 기존 메서드에도 TravelRoot 자동 생성 기능 추가
        createTravelRootsForTravelPeriod(savedTravel);

        return TravelDto.fromEntity(savedTravel);
    }

    /**
     * 여행 정보 수정
     */
    @Transactional
    public TravelDto updateTravel(Long tuid, TravelDto travelDto, String userId) {
        Travel travel = travelRepository.findById(tuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + tuid));

        // 권한 확인 (여행 작성자만 수정 가능)
        if (!travel.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("여행 수정 권한이 없습니다.");
        }

        // 날짜 변경 확인
        boolean dateChanged = false;
        LocalDate newStartDate = travelDto.getStartDate() != null ? travelDto.getStartDate() : travel.getStartDate();
        LocalDate newEndDate = travelDto.getEndDate() != null ? travelDto.getEndDate() : travel.getEndDate();

        if (!newStartDate.equals(travel.getStartDate()) || !newEndDate.equals(travel.getEndDate())) {
            dateChanged = true;
            log.info("여행 날짜 변경 감지 - 기존: {} ~ {}, 신규: {} ~ {}",
                    travel.getStartDate(), travel.getEndDate(), newStartDate, newEndDate);
        }

        // 새로운 여행 정보 생성 (불변성 유지)
        Travel updatedTravel = Travel.builder()
                .tuid(travel.getTuid())
                .user(travel.getUser())
                .location(travelDto.getLocation() != null ? travelDto.getLocation() : travel.getLocation())
                .title(travelDto.getTitle() != null ? travelDto.getTitle() : travel.getTitle())
                .startDate(newStartDate)
                .endDate(newEndDate)
                .peoples(travelDto.getPeoples() != null ? travelDto.getPeoples() : travel.getPeoples())
                .memo(travelDto.getMemo() != null ? travelDto.getMemo() : travel.getMemo())
                .totalBudget(travelDto.getTotalBudget() != null ? travelDto.getTotalBudget() : travel.getTotalBudget())
                .created(travel.getCreated())
                .travelRoots(travel.getTravelRoots())
                .travelAreas(travel.getTravelAreas())
                .travelImages(travel.getTravelImages())
                .build();

        Travel savedTravel = travelRepository.save(updatedTravel);

        // 날짜가 변경된 경우 기존 TravelRoot 삭제 후 새로 생성
        if (dateChanged) {
            log.info("날짜 변경으로 인한 여행 루트 재생성 시작");

            // 기존 TravelRoot 삭제
            List<TravelRoot> existingRoots = travelRootRepository.findByTravel(savedTravel);
            if (!existingRoots.isEmpty()) {
                log.info("기존 여행 루트 {}개 삭제", existingRoots.size());
                travelRootRepository.deleteAll(existingRoots);
            }

            // 새로운 TravelRoot 생성
            createTravelRootsForTravelPeriod(savedTravel);
        }

        return TravelDto.fromEntity(savedTravel);
    }

    /**
     * 여행 삭제
     */
    @Transactional
    public void deleteTravel(Long tuid, String userId) {
        Travel travel = travelRepository.findById(tuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + tuid));

        // 권한 확인 (여행 작성자만 삭제 가능)
        if (!travel.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("여행 삭제 권한이 없습니다.");
        }

        log.info("여행 삭제 - ID: {}, 제목: {}", travel.getTuid(), travel.getTitle());

        // Travel 삭제 시 연관된 TravelRoot들도 CASCADE로 함께 삭제됨
        travelRepository.delete(travel);

        log.info("여행 삭제 완료 - ID: {}", tuid);
    }

    /**
     * 여행 상세 정보 조회 (연관 데이터 모두 포함)
     */
    @Transactional
    public TravelDetailDto getTravelDetailById(Long tuid) {
        // 여행 정보 조회
        Travel travel = travelRepository.findById(tuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + tuid));

        // 여행 이미지 조회
        List<TravelImageDto> travelImages = travelImageRepository.findByTravel(travel).stream()
                .map(TravelImageDto::fromEntity)
                .collect(Collectors.toList());

        // 여행 결제 정보 조회
        List<TravelPaymentDto> travelPayments = travelPaymentRepository.findByTravel(travel).stream()
                .map(TravelPaymentDto::fromEntity)
                .collect(Collectors.toList());

        // 상세 DTO 생성 및 반환
        return TravelDetailDto.fromEntity(travel, travelImages, travelPayments);
    }
}