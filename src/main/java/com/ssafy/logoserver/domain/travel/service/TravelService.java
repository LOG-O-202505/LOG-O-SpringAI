package com.ssafy.logoserver.domain.travel.service;

import com.ssafy.logoserver.domain.image.dto.TravelImageDto;
import com.ssafy.logoserver.domain.image.repository.TravelImageRepository;
import com.ssafy.logoserver.domain.travel.dto.TravelCreateDto;
import com.ssafy.logoserver.domain.travel.dto.TravelDetailDto;
import com.ssafy.logoserver.domain.travel.dto.TravelDto;
import com.ssafy.logoserver.domain.travel.dto.TravelPaymentDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.repository.TravelPaymentRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRepository;
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
public class TravelService {

    private final TravelRepository travelRepository;
    private final TravelImageRepository travelImageRepository;
    private final TravelPaymentRepository travelPaymentRepository;
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
     * TravelCreateDto를 사용한 여행 생성
     */
    @Transactional
    public TravelDto createTravelFromDto(String userId, TravelCreateDto createDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 ID가 존재하지 않습니다: " + userId));

        log.info("새로운 여행 생성 - 사용자: {}, 제목: {}", user.getNickname(), createDto.getTitle());

        Travel travel = createDto.toEntity(user);
        Travel savedTravel = travelRepository.save(travel);

        log.info("여행 생성 완료 - ID: {}, 제목: {}", savedTravel.getTuid(), savedTravel.getTitle());

        return TravelDto.fromEntity(savedTravel);
    }

    /**
     * 여행 생성 (기존 메서드 유지)
     */
    @Transactional
    public TravelDto createTravel(TravelDto travelDto, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 ID가 존재하지 않습니다: " + userId));

        Travel travel = travelDto.toEntity(user);
        return TravelDto.fromEntity(travelRepository.save(travel));
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

        // 새로운 여행 정보 생성 (불변성 유지)
        Travel updatedTravel = Travel.builder()
                .tuid(travel.getTuid())
                .user(travel.getUser())
                .location(travelDto.getLocation() != null ? travelDto.getLocation() : travel.getLocation())
                .title(travelDto.getTitle() != null ? travelDto.getTitle() : travel.getTitle())
                .startDate(travelDto.getStartDate() != null ? travelDto.getStartDate() : travel.getStartDate())
                .endDate(travelDto.getEndDate() != null ? travelDto.getEndDate() : travel.getEndDate())
                .peoples(travelDto.getPeoples() != null ? travelDto.getPeoples() : travel.getPeoples())
                .memo(travelDto.getMemo() != null ? travelDto.getMemo() : travel.getMemo())
                .totalBudget(travelDto.getTotalBudget() != null ? travelDto.getTotalBudget() : travel.getTotalBudget())
                .created(travel.getCreated())
                .travelRoots(travel.getTravelRoots())
                .travelAreas(travel.getTravelAreas())
                .travelImages(travel.getTravelImages())
                .build();

        return TravelDto.fromEntity(travelRepository.save(updatedTravel));
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

        travelRepository.delete(travel);
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