package com.ssafy.logoserver.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 파일 저장소 서비스
 * 이미지 및 파일 업로드, 다운로드, 삭제 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * 버킷 존재 여부 확인 및 생성
     */
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("MinIO 버킷 생성 완료: {}", bucketName);
            } else {
                log.info("MinIO 버킷 존재 확인: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("MinIO 버킷 확인/생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("MinIO 버킷 초기화 실패", e);
        }
    }

    /**
     * 파일 업로드
     * @param file 업로드할 파일
     * @param folder 저장할 폴더 (예: "travel-images", "verification-images")
     * @return 저장된 파일의 URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
        }

        ensureBucketExists();

        // 파일명 생성 (중복 방지를 위해 타임스탬프 추가)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String fileName = folder + "/" + timestamp + "_" + System.nanoTime() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            // 파일 업로드
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            // 업로드된 파일의 URL 생성
            String fileUrl = getFileUrl(fileName);
            log.info("파일 업로드 완료 - 파일명: {}, URL: {}", fileName, fileUrl);

            return fileUrl;

        } catch (Exception e) {
            log.error("파일 업로드 실패 - 파일명: {}, 오류: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 여행 이미지 업로드
     * @param file 업로드할 이미지 파일
     * @return 저장된 이미지 URL
     */
    public String uploadTravelImage(MultipartFile file) {
        validateImageFile(file);
        return uploadFile(file, "travel-images");
    }

    /**
     * 인증 이미지 업로드
     * @param file 업로드할 이미지 파일
     * @return 저장된 이미지 URL
     */
    public String uploadVerificationImage(MultipartFile file) {
        validateImageFile(file);
        return uploadFile(file, "verification-images");
    }

    /**
     * 프로필 이미지 업로드
     * @param file 업로드할 이미지 파일
     * @return 저장된 이미지 URL
     */
    public String uploadProfileImage(MultipartFile file) {
        validateImageFile(file);
        return uploadFile(file, "profile-images");
    }

    /**
     * Base64 이미지 업로드 (기존 VerificationService와의 호환성을 위해)
     * @param base64Image Base64 인코딩된 이미지 데이터
     * @param folder 저장할 폴더
     * @return 저장된 이미지 URL
     */
    public String uploadBase64Image(String base64Image, String folder) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64 이미지 데이터가 비어있습니다.");
        }

        ensureBucketExists();

        try {
            // Base64 데이터 파싱
            String imageData = base64Image;
            String contentType = "image/jpeg"; // 기본값

            if (base64Image.contains(",")) {
                String[] parts = base64Image.split(",");
                if (parts.length == 2) {
                    String header = parts[0];
                    imageData = parts[1];

                    // Content-Type 추출
                    if (header.contains("data:")) {
                        String[] headerParts = header.split(";");
                        if (headerParts.length > 0) {
                            contentType = headerParts[0].replace("data:", "");
                        }
                    }
                }
            }

            // Base64 디코딩
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(imageData);

            // 파일명 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String extension = getExtensionFromContentType(contentType);
            String fileName = folder + "/" + timestamp + "_" + System.nanoTime() + extension;

            // 파일 업로드
            try (InputStream inputStream = new java.io.ByteArrayInputStream(decodedBytes)) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, decodedBytes.length, -1)
                        .contentType(contentType)
                        .build());
            }

            String fileUrl = getFileUrl(fileName);
            log.info("Base64 이미지 업로드 완료 - 파일명: {}, URL: {}", fileName, fileUrl);

            return fileUrl;

        } catch (Exception e) {
            log.error("Base64 이미지 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Base64 이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 삭제
     * @param fileUrl 삭제할 파일의 URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            log.warn("삭제할 파일 URL이 비어있습니다.");
            return;
        }

        try {
            // URL에서 객체명 추출
            String objectName = extractObjectNameFromUrl(fileUrl);

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            log.info("파일 삭제 완료 - 객체명: {}", objectName);

        } catch (Exception e) {
            log.error("파일 삭제 실패 - URL: {}, 오류: {}", fileUrl, e.getMessage(), e);
            throw new RuntimeException("파일 삭제 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 다운로드용 임시 URL 생성 (유효시간: 7일)
     * @param fileUrl 파일 URL
     * @return 다운로드용 임시 URL
     */
    public String getPresignedUrl(String fileUrl) {
        try {
            String objectName = extractObjectNameFromUrl(fileUrl);

            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS)
                    .build());

        } catch (Exception e) {
            log.error("임시 URL 생성 실패 - URL: {}, 오류: {}", fileUrl, e.getMessage(), e);
            throw new RuntimeException("임시 URL 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지 파일 유효성 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // 파일 크기 제한 (10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }

    /**
     * Content-Type에서 파일 확장자 추출
     */
    private String getExtensionFromContentType(String contentType) {
        switch (contentType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".webp";
            default:
                return ".jpg"; // 기본값
        }
    }

    /**
     * 파일 URL 생성
     */
    private String getFileUrl(String objectName) {
        return endpoint + "/" + bucketName + "/" + objectName;
    }

    /**
     * URL에서 객체명 추출
     */
    private String extractObjectNameFromUrl(String fileUrl) {
        String prefix = endpoint + "/" + bucketName + "/";
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }

        // 다른 형태의 URL일 경우 파일명만 추출
        String[] parts = fileUrl.split("/");
        if (parts.length >= 2) {
            // 폴더/파일명 형태로 추출
            return parts[parts.length - 2] + "/" + parts[parts.length - 1];
        }

        throw new IllegalArgumentException("유효하지 않은 파일 URL: " + fileUrl);
    }
}