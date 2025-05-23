package com.ssafy.logoserver.security.jwt;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenStore {

    private final RedisTemplate<String, String> redisTemplate;
    private final String TOKEN_PREFIX = "refresh_token:";

    public JwtTokenStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 사용자 ID와 리프레시 토큰을 저장
     */
    public void saveRefreshToken(String userId, String refreshToken, long validity) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, validity, TimeUnit.SECONDS);
    }

    /**
     * 사용자 ID로 리프레시 토큰 조회
     */
    public String getRefreshToken(String userId) {
        String key = TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 리프레시 토큰 삭제 (로그아웃 시)
     */
    public void deleteRefreshToken(String userId) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }

    /**
     * 사용자의 모든 세션 만료 (비밀번호 변경 등)
     */
    public void invalidateAllUserSessions(String userId) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }
}