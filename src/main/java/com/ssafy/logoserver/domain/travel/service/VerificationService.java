package com.ssafy.logoserver.domain.travel.service;

import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.area.repository.PlaceRepository;
import com.ssafy.logoserver.domain.image.entity.TravelImage;
import com.ssafy.logoserver.domain.image.repository.TravelImageRepository;
import com.ssafy.logoserver.domain.travel.dto.VerificationDto;
import com.ssafy.logoserver.domain.travel.dto.VerificationRequestDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelArea;
import com.ssafy.logoserver.domain.travel.entity.Verification;
import com.ssafy.logoserver.domain.travel.repository.TravelAreaRepository;
import com.ssafy.logoserver.domain.travel.repository.VerificationRepository;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import com.ssafy.logoserver.service.MinIOService;
import com.ssafy.logoserver.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final TravelAreaRepository travelAreaRepository;
    private final TravelImageRepository travelImageRepository;
    private final MinIOService minIOService;

    /**
     * 모든 인증 정보 조회
     */
    public List<VerificationDto> getAllVerifications() {
        return verificationRepository.findAll().stream()
                .map(VerificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 인증 정보 조회
     */
    public VerificationDto getVerificationById(Long vuid) {
        Verification verification = verificationRepository.findById(vuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 인증 정보가 존재하지 않습니다: " + vuid));
        return VerificationDto.fromEntity(verification);
    }

    /**
     * 사용자별 인증 정보 조회
     */
    public List<VerificationDto> getVerificationsByUserId(Long userId) {
        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + userId));

        return verificationRepository.findByUser(user).stream()
                .map(VerificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 장소별 인증 정보 조회
     */
    public List<VerificationDto> getVerificationsByPlace(Long placeId, String placeAddress) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 존재하지 않습니다: " + placeId + ", " + placeAddress));

        return verificationRepository.findByPlace(place).stream()
                .map(VerificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 특정 장소 인증 정보 조회
     */
    public VerificationDto getVerificationByUserAndPlace(Long userId, Long placeId, String placeAddress) {
        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + userId));

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 존재하지 않습니다: " + placeId + ", " + placeAddress));

        Verification verification = verificationRepository.findByUserAndPlace(user, place)
                .orElseThrow(() -> new IllegalArgumentException("해당 인증 정보가 존재하지 않습니다"));

        return VerificationDto.fromEntity(verification);
    }

    /**
     * 방문 인증 추가 (이미지 파일과 함께)
     * @param requestDto 방문 인증 요청 DTO
     * @param imageFile 인증 이미지 파일
     * @return 생성된 인증 정보 DTO
     */
    @Transactional
    public VerificationDto addVerificationWithImage(VerificationRequestDto requestDto, MultipartFile imageFile) {
        log.info("방문 인증 요청 처리 시작 - pid: {}, address: {}, 이미지: {}",
                requestDto.getPid(), requestDto.getAddress(), imageFile.getOriginalFilename());

        // 현재 로그인한 사용자 확인
        String currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("로그인이 필요합니다."));

        // 장소 확인
        Place place = placeRepository.findById(requestDto.getPid())
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 존재하지 않습니다."));

        // 인증 정보 생성
        Verification verification = Verification.builder()
                .user(user)
                .place(place)
                .star(requestDto.getStar())
                .review(requestDto.getReview())
                .build();

        Verification savedVerification = verificationRepository.save(verification);
        log.info("방문 인증 정보 저장 완료 - vuid: {}", savedVerification.getVuid());

        // 이미지 파일이 있는 경우 MinIO에 업로드 및 TravelImage 생성
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // MinIO에 이미지 업로드
                String objectKey = minIOService.uploadVerificationImage(
                        imageFile,
                        user.getUuid(),
                        savedVerification.getVuid()
                );

                // 여행 정보 찾기
                Travel travel = findTravelByPlaceAndUser(place, user);

                // TravelImage 엔티티 생성 및 저장
                TravelImage travelImage = TravelImage.builder()
                        .user(user)
                        .verification(savedVerification)
                        .travel(travel)
                        .name("방문 인증 이미지 - " + place.getName())
                        .url(objectKey)  // MinIO 객체 키 저장
                        .build();

                travelImageRepository.save(travelImage);
                log.info("여행 이미지 저장 완료 - tiuid: {}, objectKey: {}",
                        travelImage.getTiuid(), objectKey);

            } catch (Exception e) {
                log.error("이미지 업로드 중 오류 발생", e);
                // 인증 데이터는 이미 저장되었으므로, 이미지 업로드 실패만 로그 기록
                // 필요에 따라 인증 데이터도 롤백할 수 있음
            }
        }

        return VerificationDto.fromEntity(savedVerification);
    }

    /**
     * 기존 addVerification 메서드 (하위 호환성을 위해 유지하되 Deprecated 처리)
     * @deprecated addVerificationWithImage 메서드 사용 권장
     */
    @Deprecated
    @Transactional
    public VerificationDto addVerification(VerificationRequestDto requestDto) {
        log.warn("Deprecated addVerification method called. Use addVerificationWithImage instead.");
        throw new UnsupportedOperationException("이 메서드는 더 이상 지원되지 않습니다. addVerificationWithImage를 사용하세요.");
    }

    /**
     * 기존 Base64 이미지 저장 메서드 (더 이상 사용하지 않음)
     * @deprecated MinIO 업로드 방식으로 변경됨
     */
    @Deprecated
    private void saveImageFromBase64(String base64Image, Verification verification) {
        log.warn("Deprecated saveImageFromBase64 method called. MinIO upload is used instead.");
        throw new UnsupportedOperationException("Base64 이미지 저장은 더 이상 지원되지 않습니다.");
    }

    /**
     * 장소와 사용자를 기반으로 여행 찾기
     * @param place 장소 엔티티
     * @param user 사용자 엔티티
     * @return 찾은 여행 엔티티
     */
    private Travel findTravelByPlaceAndUser(Place place, User user) {
        // 사용자의 여행 지역 중 해당 장소의 지역과 같은 지역을 가진 여행 지역 찾기
        List<TravelArea> userTravelAreas = travelAreaRepository.findByUser(user);

        for (TravelArea travelArea : userTravelAreas) {
            if (travelArea.getArea() != null &&
                    travelArea.getArea().equals(place.getArea())) {
                return travelArea.getTravel();
            }
        }

        throw new IllegalArgumentException("해당 장소와 연관된 여행을 찾을 수 없습니다.");
    }
}