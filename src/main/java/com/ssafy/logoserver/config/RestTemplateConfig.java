package com.ssafy.logoserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정 클래스
 * PATCH 메서드 지원을 위해 Apache HttpClient를 사용하도록 설정
 */
@Configuration
public class RestTemplateConfig {

    /**
     * PATCH 메서드를 지원하는 RestTemplate Bean 생성
     * Apache HttpClient를 사용하여 모든 HTTP 메서드를 지원
     *
     * @return PATCH 메서드를 지원하는 RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        // Apache HttpClient를 사용하는 RequestFactory 생성
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        // 연결 타임아웃 설정 (10초)
        factory.setConnectTimeout(10000);

        // 읽기 타임아웃 설정 (30초)
        factory.setReadTimeout(30000);

        return new RestTemplate(factory);
    }
}