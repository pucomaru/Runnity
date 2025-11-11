package com.runnity.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 서버 헬스 체크 서비스
 * 
 * Redis를 통해 각 WebSocket 서버의 상태를 추적합니다.
 * - WebSocket 서버가 주기적으로 Redis에 heartbeat를 기록 (ws_health:서버URL = "UP")
 * - 비즈니스 서버는 Redis를 조회하여 서버 상태를 확인
 * 
 * Note: Heartbeat 기록은 WebSocket 서버에서 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketHealthCheckService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String HEALTH_KEY_PREFIX = "ws_health:";
    private static final Duration CIRCUIT_BREAKER_TTL = Duration.ofSeconds(30);

    /**
     * WebSocket 서버가 살아있는지 확인
     * 
     * Redis에 저장된 heartbeat 정보를 조회하여 판단합니다.
     * WebSocket 서버가 주기적으로 기록한 heartbeat가 있으면 살아있는 것으로 간주합니다.
     * 
     * @param serverUrl WebSocket 서버 URL
     * @return 서버가 살아있으면 true, 다운되었거나 응답 없으면 false
     */
    public boolean isHealthy(String serverUrl) {
        try {
            String key = HEALTH_KEY_PREFIX + normalizeUrl(serverUrl);
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                log.warn("WebSocket 서버 heartbeat 없음: {}", serverUrl);
                return false;
            }
            
            return "UP".equals(value);
        } catch (Exception e) {
            log.error("WebSocket 서버 health check 실패: {}", serverUrl, e);
            return false;
        }
    }

    /**
     * WebSocket 서버를 일시적으로 차단 (Circuit Breaker)
     * 
     * 연속적인 연결 실패 등의 이유로 서버를 일정 시간 동안 차단합니다.
     * 차단된 서버는 TTL 이후 자동으로 재활성화됩니다.
     * 
     * @param serverUrl WebSocket 서버 URL
     */
    public void markAsDown(String serverUrl) {
        try {
            String key = HEALTH_KEY_PREFIX + normalizeUrl(serverUrl);
            redisTemplate.opsForValue().set(key, "DOWN", CIRCUIT_BREAKER_TTL.getSeconds(), TimeUnit.SECONDS);
            log.warn("WebSocket 서버 차단: {} ({}초간)", serverUrl, CIRCUIT_BREAKER_TTL.getSeconds());
        } catch (Exception e) {
            log.error("서버 차단 실패: {}", serverUrl, e);
        }
    }

    /**
     * URL 정규화 (Redis 키로 사용)
     * 
     * @param url WebSocket 서버 URL
     * @return 정규화된 URL (예: ws://localhost:8085 → localhost:8085)
     */
    private String normalizeUrl(String url) {
        return url.replace("ws://", "").replace("wss://", "");
    }
}

