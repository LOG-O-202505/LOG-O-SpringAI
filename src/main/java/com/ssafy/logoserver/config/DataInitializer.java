package com.ssafy.logoserver.config;

import com.ssafy.logoserver.domain.area.entity.Area;
import com.ssafy.logoserver.domain.area.repository.AreaRepository;
import com.ssafy.logoserver.domain.user.entity.User;
import com.ssafy.logoserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("dev") // 개발 환경에서만 실행
public class DataInitializer {

    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // 테스트 계정 생성
            if (userRepository.count() == 0) {
                log.info("샘플 사용자 데이터 생성 중...");

                User adminUser = User.builder()
                        .id("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .name("관리자")
                        .nickname("어드민")
                        .birthday(LocalDate.of(1990, 1, 1))
                        .address("서울특별시 강남구")
                        .phone("010-1234-5678")
                        .role(User.Role.ADMIN)
                        .build();

                User testUser = User.builder()
                        .id("user")
                        .password(passwordEncoder.encode("user123"))
                        .name("테스트")
                        .nickname("유저")
                        .birthday(LocalDate.of(1995, 5, 5))
                        .address("서울특별시 서초구")
                        .phone("010-9876-5432")
                        .role(User.Role.USER)
                        .build();

                userRepository.saveAll(Arrays.asList(adminUser, testUser));
                log.info("샘플 사용자 데이터 생성 완료!");
            }

            // 샘플 지역 데이터 생성
            if (areaRepository.count() == 0) {
                log.info("샘플 지역 데이터 생성 중...");

                List<Area> areas = Arrays.asList(
                        Area.builder().areaName("서울").build(),
                        Area.builder().areaName("부산").build(),
                        Area.builder().areaName("제주").build(),
                        Area.builder().areaName("강원").build(),
                        Area.builder().areaName("경기").build()
                );

                areaRepository.saveAll(areas);
                log.info("샘플 지역 데이터 생성 완료!");
            }
        };
    }
}