package com.ssafy.logoserver.domain.travel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "travel_payments")
@AllArgsConstructor
@NoArgsConstructor
public class TravelPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tpuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_id", nullable = false)
    private Travel travel;

    private String history;

    private Integer cost;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

}
