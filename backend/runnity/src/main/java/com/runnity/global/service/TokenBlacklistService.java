package com.runnity.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 토큰 블랙리스트 관리 서비스
 * 로그아웃된 토큰을 Redis에 저장하여 재사용 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    /**
     * 토큰을 블랙리스트에 추가 (로그아웃 시)
     * @param token JWT 토큰
     */
    public void addToBlacklist(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(
                    key,
                    "logged_out"
            );
        } catch (Exception e) {
            log.error("토큰 블랙리스트 등록 실패", e);
            throw new RuntimeException("토큰 블랙리스트 등록 실패", e);
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true
     */
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(key);

            if (exists != null && exists) {
                log.warn("블랙리스트 토큰 감지");
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("블랙리스트 조회 실패", e);
            return false;
        }
    }
}
