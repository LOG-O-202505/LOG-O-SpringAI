package com.ssafy.logoserver.domain.image.service;

import com.ssafy.logoserver.domain.image.dto.TravelImageDto;
import com.ssafy.logoserver.domain.image.entity.TravelImage;
import com.ssafy.logoserver.domain.image.repository.TravelImageRepository;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.repository.TravelRepository;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import com.ssafy.logoserver.service.MinIOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TravelImageService {

    private final TravelImageRepository travelImageRepository;
    private final UserRepository userRepository;
    private final TravelRepository travelRepository;
    private final MinIOService minIOService;

    /**
     * 특정 여행의 가장 최근 이미지 URL 조회
     * 사용자 프로필 페이지에서 여행 썸네일로 사용하기 위한 메서드
     * @param travelId 여행 ID
     * @param expiryMinutes URL 만료 시간 (분)
     * @return 가장 최근 이미지의 Presigned URL, 이미지가 없으면 null
     */
    public String getLatestTravelImageUrl(Long travelId, int expiryMinutes) {
        log.info("여행의 최근 이미지 URL 조회 시작 - travelId: {}, 만료시간: {}분", travelId, expiryMinutes);

        // 해당 여행의 모든 이미지 조회
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelId));

        // ✅ travel이 null이 아닌 이미지들만 조회
        List<TravelImage> travelImages = travelImageRepository.findByTravel(travel)
                .stream()
                .filter(image -> image.getTravel() != null)  // travel이 null이 아닌 것만 필터링
                .toList();

        if (travelImages.isEmpty()) {
            log.info("여행에 등록된 이미지가 없습니다 - travelId: {}", travelId);
            return null;
        }

        try {
            // TravelImage 엔티티의 tiuid를 기준으로 가장 최근 이미지 찾기
            // tiuid가 AUTO_INCREMENT라서 높은 값일수록 최근에 등록된 이미지
            TravelImage latestImage = travelImages.stream()
                    .max(Comparator.comparing(TravelImage::getTiuid))
                    .orElse(null);

            if (latestImage == null) {
                log.warn("가장 최근 이미지를 찾을 수 없습니다 - travelId: {}", travelId);
                return null;
            }

            // MinIO 객체 키로 Presigned URL 생성
            String objectKey = latestImage.getUrl();
            String presignedUrl = minIOService.generatePresignedUrl(objectKey, expiryMinutes);

            log.info("여행 최근 이미지 URL 생성 완료 - travelId: {}, tiuid: {}, objectKey: {}",
                    travelId, latestImage.getTiuid(), objectKey);

            return presignedUrl;

        } catch (Exception e) {
            log.error("여행 최근 이미지 URL 생성 실패 - travelId: {}", travelId, e);
            // 개별 이미지 URL 생성 실패 시 null 반환
            return null;
        }
    }

    /**
     * 여러 여행의 최근 이미지 URL을 일괄 조회
     * 사용자 프로필 페이지에서 효율적인 조회를 위한 메서드
     * @param travelIds 여행 ID 목록
     * @param expiryMinutes URL 만료 시간 (분)
     * @return 여행 ID를 키로 하고, 최근 이미지 URL을 값으로 하는 Map
     */
    public Map<Long, String> getLatestTravelImageUrls(List<Long> travelIds, int expiryMinutes) {
        log.info("여러 여행의 최근 이미지 URL 일괄 조회 시작 - 여행 수: {}, 만료시간: {}분",
                travelIds.size(), expiryMinutes);

        Map<Long, String> result = new HashMap<>();

        for (Long travelId : travelIds) {
            try {
                String latestImageUrl = getLatestTravelImageUrl(travelId, expiryMinutes);
                result.put(travelId, latestImageUrl); // null 값도 저장 (이미지 없음을 표시)

                log.debug("여행 {} 최근 이미지 URL: {}", travelId,
                        latestImageUrl != null ? "생성됨" : "없음");

            } catch (Exception e) {
                log.error("여행 {} 최근 이미지 URL 조회 실패", travelId, e);
                result.put(travelId, null); // 오류 발생 시 null 저장
            }
        }

        log.info("여러 여행의 최근 이미지 URL 일괄 조회 완료 - 성공: {}개, 전체: {}개",
                result.size(), travelIds.size());

        return result;
    }

    /**
     * 기본 만료 시간(30분)으로 최근 여행 이미지 URL 조회
     * @param travelId 여행 ID
     * @return 가장 최근 이미지의 Presigned URL
     */
    public String getLatestTravelImageUrl(Long travelId) {
        return getLatestTravelImageUrl(travelId, 30); // 기본 30분 만료
    }

    // 기존 메서드들은 그대로 유지...

    /**
     * 모든 여행 이미지 조회
     */
    public List<TravelImageDto> getAllTravelImages() {
        return travelImageRepository.findAll().stream()
                .map(TravelImageDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행 이미지 조회
     */
    public TravelImageDto getTravelImageById(Long tiuid) {
        TravelImage travelImage = travelImageRepository.findById(tiuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 이미지가 존재하지 않습니다: " + tiuid));
        return TravelImageDto.fromEntity(travelImage);
    }

    /**
     * 특정 사용자의 여행 이미지 조회
     */
    public List<TravelImageDto> getTravelImagesByUserId(Long userId) {
        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + userId));

        return travelImageRepository.findByUser(user).stream()
                .map(TravelImageDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 여행의 이미지 조회
     */
    public List<TravelImageDto> getTravelImagesByTravelId(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelId));

        return travelImageRepository.findByTravel(travel).stream()
                .map(TravelImageDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 특정 여행의 이미지 조회
     */
    public List<TravelImageDto> getTravelImagesByUserAndTravelId(Long userId, Long travelId) {
        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + userId));

        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelId));

        return travelImageRepository.findByUserAndTravel(user, travel).stream()
                .map(TravelImageDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 여행 이미지 생성
     */
    @Transactional
    public TravelImageDto createTravelImage(TravelImageDto travelImageDto) {
        User user = userRepository.findByUuid(travelImageDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + travelImageDto.getUserId()));

        Travel travel = travelRepository.findById(travelImageDto.getTravelId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelImageDto.getTravelId()));

        // 권한 확인 (여행 작성자만 이미지 추가 가능)
        if (!travel.getUser().getUuid().equals(user.getUuid())) {
            throw new IllegalArgumentException("여행 이미지 생성 권한이 없습니다.");
        }

        TravelImage travelImage = travelImageDto.toEntity(user, travel);
        return TravelImageDto.fromEntity(travelImageRepository.save(travelImage));
    }

    /**
     * 여행 이미지 수정
     */
    @Transactional
    public TravelImageDto updateTravelImage(Long tiuid, TravelImageDto travelImageDto) {
        TravelImage travelImage = travelImageRepository.findById(tiuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 이미지가 존재하지 않습니다: " + tiuid));

        // 권한 확인 (여행 이미지 작성자만 수정 가능)
        if (!travelImage.getUser().getUuid().equals(travelImageDto.getUserId())) {
            throw new IllegalArgumentException("여행 이미지 수정 권한이 없습니다.");
        }

        // 여행 변경이 필요한 경우
        Travel travel = travelImage.getTravel();
        if (travelImageDto.getTravelId() != null) {
            // ✅ travelId가 있는 경우에만 여행 조회
            travel = travelRepository.findById(travelImageDto.getTravelId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelImageDto.getTravelId()));
        } else {
            // ✅ travelId가 null인 경우 travel을 null로 설정
            travel = null;
        }

        // 새로운 여행 이미지 정보 생성 (불변성 유지)
        TravelImage updatedTravelImage = TravelImage.builder()
                .tiuid(travelImage.getTiuid())
                .user(travelImage.getUser())
                .travel(travel)  // ✅ null일 수 있음
                .verification(travelImage.getVerification())
                .name(travelImageDto.getName() != null ? travelImageDto.getName() : travelImage.getName())
                .url(travelImageDto.getUrl() != null ? travelImageDto.getUrl() : travelImage.getUrl())
                .build();

        return TravelImageDto.fromEntity(travelImageRepository.save(updatedTravelImage));
    }

    /**
     * 여행 이미지 삭제
     */
    @Transactional
    public void deleteTravelImage(Long tiuid, Long userId) {
        TravelImage travelImage = travelImageRepository.findById(tiuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 이미지가 존재하지 않습니다: " + tiuid));

        // 권한 확인 (여행 이미지 작성자만 삭제 가능)
        if (!travelImage.getUser().getUuid().equals(userId)) {
            throw new IllegalArgumentException("여행 이미지 삭제 권한이 없습니다.");
        }

        travelImageRepository.delete(travelImage);
    }

    /**
     * 여행 이미지 조회 URL 생성
     * MinIO에 저장된 이미지에 대한 Presigned URL을 생성합니다.
     *
     * @param tiuid 여행 이미지 ID
     * @param expiryMinutes URL 만료 시간 (분)
     * @return Presigned URL
     */
    public String getTravelImageUrl(Long tiuid, int expiryMinutes) {
        log.info("여행 이미지 URL 생성 요청 - tiuid: {}, 만료시간: {}분", tiuid, expiryMinutes);

        TravelImage travelImage = travelImageRepository.findById(tiuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 이미지가 존재하지 않습니다: " + tiuid));

        // MinIO 객체 키로 Presigned URL 생성
        String objectKey = travelImage.getUrl(); // url 필드에 객체 키가 저장됨
        String presignedUrl = minIOService.generatePresignedUrl(objectKey, expiryMinutes);

        log.info("여행 이미지 URL 생성 완료 - tiuid: {}, objectKey: {}", tiuid, objectKey);
        return presignedUrl;
    }

    /**
     * 여행별 이미지 URL 목록 생성
     * 특정 여행의 모든 이미지에 대한 Presigned URL 목록을 생성합니다.
     *
     * @param travelId 여행 ID
     * @param expiryMinutes URL 만료 시간 (분)
     * @return 이미지 정보와 URL이 포함된 맵 리스트
     */
    public List<Map<String, Object>> getTravelImageUrls(Long travelId, int expiryMinutes) {
        log.info("여행별 이미지 URL 목록 생성 요청 - travelId: {}, 만료시간: {}분", travelId, expiryMinutes);

        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelId));

        List<TravelImage> travelImages = travelImageRepository.findByTravel(travel);
        List<Map<String, Object>> result = new ArrayList<>();

        for (TravelImage travelImage : travelImages) {
            try {
                String objectKey = travelImage.getUrl();
                String presignedUrl = minIOService.generatePresignedUrl(objectKey, expiryMinutes);

                Map<String, Object> imageInfo = new HashMap<>();
                imageInfo.put("tiuid", travelImage.getTiuid());
                imageInfo.put("name", travelImage.getName());
                imageInfo.put("imageUrl", presignedUrl);
                imageInfo.put("expiryMinutes", expiryMinutes);

                result.add(imageInfo);

                log.debug("여행 이미지 URL 생성 - tiuid: {}, name: {}",
                        travelImage.getTiuid(), travelImage.getName());

            } catch (Exception e) {
                log.error("여행 이미지 URL 생성 실패 - tiuid: {}", travelImage.getTiuid(), e);
                // 개별 이미지 URL 생성 실패 시에도 다른 이미지들은 계속 처리
            }
        }

        log.info("여행별 이미지 URL 목록 생성 완료 - travelId: {}, 총 {}개", travelId, result.size());
        return result;
    }

    /**
     * 사용자별 이미지 URL 목록 생성
     * 특정 사용자의 모든 여행 이미지에 대한 Presigned URL 목록을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param expiryMinutes URL 만료 시간 (분)
     * @return 이미지 정보와 URL이 포함된 맵 리스트
     */
    public List<Map<String, Object>> getUserImageUrls(Long userId, int expiryMinutes) {
        log.info("사용자별 이미지 URL 목록 생성 요청 - userId: {}, 만료시간: {}분", userId, expiryMinutes);

        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + userId));

        List<TravelImage> travelImages = travelImageRepository.findByUser(user);
        List<Map<String, Object>> result = new ArrayList<>();

        for (TravelImage travelImage : travelImages) {
            try {
                String objectKey = travelImage.getUrl();
                String presignedUrl = minIOService.generatePresignedUrl(objectKey, expiryMinutes);

                Map<String, Object> imageInfo = new HashMap<>();
                imageInfo.put("tiuid", travelImage.getTiuid());
                imageInfo.put("name", travelImage.getName());
                // ✅ travel이 null일 수 있으므로 null 체크 추가
                imageInfo.put("travelId", travelImage.getTravel() != null ?
                        travelImage.getTravel().getTuid() : null);
                imageInfo.put("imageUrl", presignedUrl);
                imageInfo.put("expiryMinutes", expiryMinutes);

                result.add(imageInfo);

                log.debug("사용자 이미지 URL 생성 - tiuid: {}, travelId: {}",
                        travelImage.getTiuid(), travelImage.getTravel().getTuid());

            } catch (Exception e) {
                log.error("사용자 이미지 URL 생성 실패 - tiuid: {}", travelImage.getTiuid(), e);
                // 개별 이미지 URL 생성 실패 시에도 다른 이미지들은 계속 처리
            }
        }

        log.info("사용자별 이미지 URL 목록 생성 완료 - userId: {}, 총 {}개", userId, result.size());
        return result;
    }

    /**
     * 기본 만료 시간(30분)으로 이미지 URL 생성
     *
     * @param tiuid 여행 이미지 ID
     * @return Presigned URL
     */
    public String getTravelImageUrl(Long tiuid) {
        return getTravelImageUrl(tiuid, 30); // 기본 30분 만료
    }
}