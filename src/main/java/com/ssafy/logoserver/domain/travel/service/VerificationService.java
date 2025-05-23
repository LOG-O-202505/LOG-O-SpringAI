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
import com.ssafy.logoserver.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 방문 인증 추가
     * @param requestDto 방문 인증 요청 DTO
     * @return 생성된 인증 정보 DTO
     */
    @Transactional
    public VerificationDto addVerification(VerificationRequestDto requestDto) {
        log.info("방문 인증 요청 처리 시작 - pid: {}, address: {}", requestDto.getPid(), requestDto.getAddress());

        // 현재 로그인한 사용자 확인
        String currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findByUserId(currentUserId)
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

        // 이미지 처리 (필요시)
        if (requestDto.getImage() != null && !requestDto.getImage().isEmpty()) {
            // 이미지 저장 로직
            saveImageFromBase64(requestDto.getImage(), savedVerification);
        }

        return VerificationDto.fromEntity(savedVerification);
    }

    /**
     * Base64 인코딩된 이미지를 저장
     * @param base64Image Base64 인코딩된 이미지 데이터
     * @param verification 방문 인증 엔티티
     */
    private void saveImageFromBase64(String base64Image, Verification verification) {
        try {
            log.info("인증 이미지 저장 시작 - vuid: {}", verification.getVuid());

            // Base64 디코딩
            String imageData = base64Image;
            if (base64Image.contains(",")) {
                imageData = base64Image.split(",")[1];
            }

            byte[] decodedBytes = Base64.getDecoder().decode(imageData);

            // 이미지 파일 저장 경로
            String fileName = "verification_" + verification.getVuid() + ".jpg";
            String filePath = "uploads/verifications/" + fileName;

            // 디렉토리 생성
            File directory = new File("uploads/verifications");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 파일 저장
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                outputStream.write(decodedBytes);
            }

            log.info("인증 이미지 파일 저장 완료 - 경로: {}", filePath);

            Place place = verification.getPlace();
            User user = verification.getUser();

            // 여행 찾기
            Travel travel = findTravelByPlaceAndUser(place, user);

            // TravelImage 엔티티 생성 및 저장
            TravelImage travelImage = TravelImage.builder()
                    .user(verification.getUser())
                    .verification(verification)
                    .travel(travel)
                    .name("방문 인증 이미지")
                    .url(filePath)
                    .build();

            travelImageRepository.save(travelImage);
            log.info("여행 이미지 저장 완료 - tiuid: {}", travelImage.getTiuid());

        } catch (Exception e) {
            log.error("이미지 저장 중 오류 발생", e);
            throw new RuntimeException("이미지 저장 중 오류 발생: " + e.getMessage(), e);
        }
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