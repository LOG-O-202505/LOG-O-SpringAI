package com.ssafy.logoserver.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 객체 스토리지 서비스
 * 이미지 파일의 업로드, 다운로드 URL 생성, 삭제 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinIOService {

    private final MinioClient minioClient;

    /**
     * MinIO 버킷 이름
     */
    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 이미지 파일을 MinIO에 업로드하고 객체 키를 반환
     *
     * @param file 업로드할 이미지 파일
     * @param userId 사용자 ID (폴더 구조 생성용)
     * @param verificationId 인증 ID (파일명 생성용)
     * @return MinIO 객체 키 (파일 경로)
     * @throws RuntimeException 업로드 실패 시
     */
    public String uploadVerificationImage(MultipartFile file, Long userId, Long verificationId) {
        try {
            // 파일 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);

            // 객체 키 생성: verifications/{userId}/{verificationId}_{timestamp}_{uuid}.{extension}
            String objectKey = generateVerificationObjectKey(userId, verificationId, fileExtension);

            log.info("이미지 업로드 시작 - 파일명: {}, 객체키: {}, 크기: {} bytes",
                    originalFilename, objectKey, file.getSize());

            // MinIO에 파일 업로드
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)                    // 버킷명
                                .object(objectKey)                     // 객체 키
                                .stream(inputStream, file.getSize(), -1)  // 입력 스트림과 크기
                                .contentType(file.getContentType())    // 콘텐츠 타입
                                .build()
                );
            }

            log.info("이미지 업로드 완료 - 객체키: {}", objectKey);
            return objectKey;

        } catch (Exception e) {
            log.error("이미지 업로드 실패 - 사용자: {}, 인증: {}", userId, verificationId, e);
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지 조회를 위한 Presigned URL 생성
     *
     * @param objectKey MinIO 객체 키
     * @param expiryMinutes URL 만료 시간 (분)
     * @return Presigned URL
     * @throws RuntimeException URL 생성 실패 시
     */
    public String generatePresignedUrl(String objectKey, int expiryMinutes) {
        try {
            log.info("Presigned URL 생성 시작 - 객체키: {}, 만료시간: {}분", objectKey, expiryMinutes);

            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)           // HTTP 메서드
                            .bucket(bucketName)           // 버킷명
                            .object(objectKey)            // 객체 키
                            .expiry(expiryMinutes, TimeUnit.MINUTES)  // 만료 시간
                            .build()
            );

            log.info("Presigned URL 생성 완료 - 객체키: {}", objectKey);
            return presignedUrl;

        } catch (Exception e) {
            log.error("Presigned URL 생성 실패 - 객체키: {}", objectKey, e);
            throw new RuntimeException("이미지 URL 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * MinIO에서 이미지 파일 삭제
     *
     * @param objectKey 삭제할 객체 키
     * @throws RuntimeException 삭제 실패 시
     */
    public void deleteImage(String objectKey) {
        try {
            log.info("이미지 삭제 시작 - 객체키: {}", objectKey);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );

            log.info("이미지 삭제 완료 - 객체키: {}", objectKey);

        } catch (Exception e) {
            log.error("이미지 삭제 실패 - 객체키: {}", objectKey, e);
            throw new RuntimeException("이미지 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 인증 이미지용 객체 키 생성
     * 형식: verifications/{userId}/{verificationId}_{timestamp}_{uuid}.{extension}
     *
     * @param userId 사용자 ID
     * @param verificationId 인증 ID
     * @param fileExtension 파일 확장자
     * @return 생성된 객체 키
     */
    private String generateVerificationObjectKey(Long userId, Long verificationId, String fileExtension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8); // UUID의 앞 8자리만 사용

        return String.format("verifications/%d/%d_%s_%s.%s",
                userId, verificationId, timestamp, uuid, fileExtension);
    }

    /**
     * 파일명에서 확장자 추출
     *
     * @param filename 파일명
     * @return 확장자 (점 제외)
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg"; // 기본 확장자
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }

        return "jpg"; // 기본 확장자
    }

    /**
     * 기본 Presigned URL 생성 (30분 만료)
     *
     * @param objectKey MinIO 객체 키
     * @return Presigned URL
     */
    public String generatePresignedUrl(String objectKey) {
        return generatePresignedUrl(objectKey, 30); // 기본 30분 만료
    }
}