package com.ssafy.logoserver.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 설정 클래스
 * MinIO 클라이언트 빈을 생성하고 연결을 관리합니다.
 */
@Configuration
@Slf4j
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * MinIO 클라이언트 빈 생성
     * @return MinioClient 인스턴스
     */
    @Bean
    public MinioClient minioClient() {
        log.info("MinIO 클라이언트 초기화 - endpoint: {}", endpoint);
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}