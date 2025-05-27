package com.ssafy.logoserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정 클래스
 * 기본 RestTemplate 사용 (PATCH 메서드도 지원됨)
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 기본 RestTemplate Bean 생성
     * Spring Boot 3.x에서는 기본적으로 PATCH 메서드를 지원
     *
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}