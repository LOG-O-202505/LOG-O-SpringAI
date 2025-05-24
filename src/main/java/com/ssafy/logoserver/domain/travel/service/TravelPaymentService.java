package com.ssafy.logoserver.domain.travel.service;

import com.ssafy.logoserver.domain.travel.dto.TravelPaymentDto;
import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelPayment;
import com.ssafy.logoserver.domain.travel.repository.TravelPaymentRepository;
import com.ssafy.logoserver.domain.travel.repository.TravelRepository;
import com.ssafy.logoserver.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TravelPaymentService {

    private final TravelPaymentRepository travelPaymentRepository;
    private final TravelRepository travelRepository;

    /**
     * 여행 ID로 결제 내역 목록 조회
     */
    public List<TravelPaymentDto> getTravelPaymentsByTravelId(Long travelId) {
        // 여행 조회
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelId));

        // 결제 내역 조회 및 DTO 변환
        return travelPaymentRepository.findByTravel(travel).stream()
                .map(TravelPaymentDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 결제 내역 추가
     * @param travelId 여행 ID
     * @param cost 결제 금액
     * @param history 결제 내역
     * @param paymentTime 결제 시간
     * @return 추가된 결제 내역 DTO
     */
    @Transactional
    public TravelPaymentDto addTravelPayment(Long travelId, Integer cost, String history, LocalDateTime paymentTime) {
        // 현재 로그인한 사용자 ID 확인
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        // 여행 조회
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행이 존재하지 않습니다: " + travelId));

        // 권한 확인 (여행 작성자만 결제 내역 추가 가능)
        if (!travel.getUser().getId().equals(currentUserId)) {
            throw new IllegalStateException("결제 내역 추가 권한이 없습니다.");
        }

        // 결제 시간이 null인 경우 현재 시간으로 설정
        if (paymentTime == null) {
            paymentTime = LocalDateTime.now();
        }

        // 결제 내역 생성
        TravelPayment travelPayment = new TravelPayment(null, travel, history, cost, paymentTime);

        // 저장 및 DTO 변환하여 반환
        return TravelPaymentDto.fromEntity(travelPaymentRepository.save(travelPayment));
    }

    /**
     * 결제 내역 수정
     * @param tpuid 결제 내역 ID
     * @param cost 결제 금액
     * @param history 결제 내역
     * @param paymentTime 결제 시간
     * @return 수정된 결제 내역 DTO
     */
    @Transactional
    public TravelPaymentDto updateTravelPayment(Long tpuid, Integer cost, String history, LocalDateTime paymentTime) {
        // 현재 로그인한 사용자 ID 확인
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        // 결제 내역 조회
        TravelPayment travelPayment = travelPaymentRepository.findById(tpuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제 내역이 존재하지 않습니다: " + tpuid));

        // 권한 확인 (여행 작성자만 결제 내역 수정 가능)
        if (!travelPayment.getTravel().getUser().getId().equals(currentUserId)) {
            throw new IllegalStateException("결제 내역 수정 권한이 없습니다.");
        }

        // 결제 내역 수정
        TravelPayment updatedTravelPayment = new TravelPayment(
                travelPayment.getTpuid(),
                travelPayment.getTravel(),
                history != null ? history : travelPayment.getHistory(),
                cost != null ? cost : travelPayment.getCost(),
                paymentTime != null ? paymentTime : travelPayment.getPaymentTime()
        );

        // 저장 및 DTO 변환하여 반환
        return TravelPaymentDto.fromEntity(travelPaymentRepository.save(updatedTravelPayment));
    }

    /**
     * 결제 내역 삭제
     * @param tpuid 결제 내역 ID
     */
    @Transactional
    public void deleteTravelPayment(Long tpuid) {
        // 현재 로그인한 사용자 ID 확인
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        // 결제 내역 조회
        TravelPayment travelPayment = travelPaymentRepository.findById(tpuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제 내역이 존재하지 않습니다: " + tpuid));

        // 권한 확인 (여행 작성자만 결제 내역 삭제 가능)
        if (!travelPayment.getTravel().getUser().getId().equals(currentUserId)) {
            throw new IllegalStateException("결제 내역 삭제 권한이 없습니다.");
        }

        // 결제 내역 삭제
        travelPaymentRepository.delete(travelPayment);
    }
}