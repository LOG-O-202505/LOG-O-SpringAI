package com.ssafy.logoserver.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 클라이언트 설정 클래스
 * MinIO 객체 스토리지 서버와의 연결을 위한 클라이언트 Bean을 생성합니다.
 */
@Configuration
@Slf4j
public class MinIOConfig {

    /**
     * MinIO 서버 엔드포인트 URL
     */
    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * MinIO 접근 키 (사용자명)
     */
    @Value("${minio.access-key}")
    private String accessKey;

    /**
     * MinIO 비밀 키 (비밀번호)
     */
    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * MinIO 클라이언트 Bean 생성
     *
     * @return MinioClient 이미지 업로드/다운로드에 사용할 MinIO 클라이언트
     */
    @Bean
    public MinioClient minioClient() {
        log.info("MinIO 클라이언트 초기화 - 엔드포인트: {}", endpoint);

        return MinioClient.builder()
                .endpoint(endpoint)          // MinIO 서버 주소
                .credentials(accessKey, secretKey)  // 인증 정보
                .build();
    }
}