package com.ssafy.logoserver.domain.travel.service;

import com.ssafy.logoserver.domain.area.entity.Place;
import com.ssafy.logoserver.domain.area.entity.PlacePK;
import com.ssafy.logoserver.domain.area.repository.PlaceRepository;
import com.ssafy.logoserver.domain.travel.dto.VerificationDto;
import com.ssafy.logoserver.domain.travel.entity.Verification;
import com.ssafy.logoserver.domain.travel.repository.VerificationRepository;
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
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

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
        PlacePK placePK = new PlacePK(placeId, placeAddress);
        Place place = placeRepository.findById(placePK)
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

        PlacePK placePK = new PlacePK(placeId, placeAddress);
        Place place = placeRepository.findById(placePK)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소가 존재하지 않습니다: " + placeId + ", " + placeAddress));

        Verification verification = verificationRepository.findByUserAndPlace(user, place)
                .orElseThrow(() -> new IllegalArgumentException("해당 인증 정보가 존재하지 않습니다"));

        return VerificationDto.fromEntity(verification);
    }

    // 추가적인 메서드 구현 필요
}