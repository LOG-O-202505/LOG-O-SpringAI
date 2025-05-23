package com.ssafy.logoserver.domain.travel.repository;

import com.ssafy.logoserver.domain.travel.entity.Travel;
import com.ssafy.logoserver.domain.travel.entity.TravelPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelPaymentRepository extends JpaRepository<TravelPayment, Long> {
    List<TravelPayment> findByTravel(Travel travel);
}
