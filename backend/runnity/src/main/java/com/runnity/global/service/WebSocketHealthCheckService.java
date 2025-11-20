package com.runnity.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * WebSocket 서버 헬스 체크 서비스
 * 
 * Redis를 통해 각 WebSocket 서버의 상태를 추적합니다.
 * - WebSocket 서버가 주기적으로 Redis에 TTL 기반 heartbeat를 갱신 (10초 주기)
 * - 비즈니스 서버는 Redis Key EXISTS 여부만으로 서버 상태를 확인
 * - TTL이 만료되면 자동으로 서버가 다운된 것으로 간주
 * 
 * Heartbeat 기록은 WebSocket 서버에서 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketHealthCheckService {

    @Qualifier("stringRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${websocket.health.circuit-breaker-ttl:30}")
    private int circuitBreakerTtl;

    private static final String HEALTH_KEY_PREFIX = "ws_health:";
    private static final String CIRCUIT_BREAKER_PREFIX = "ws_circuit_breaker:";

    /**
     * WebSocket 서버가 살아있는지 확인
     * 
     * Redis Key의 존재 여부만으로 판단합니다.
     * WebSocket 서버가 주기적으로 TTL을 갱신하므로, Key가 존재하면 살아있는 것입니다.
     * 
     * @param serverUrl WebSocket 서버 URL
     * @return 서버가 살아있으면 true, 다운되었거나 응답 없으면 false
     */
    public boolean isHealthy(String serverUrl) {
        try {
            String normalizedUrl = normalizeUrl(serverUrl);
            
            // Circuit Breaker로 차단된 서버인지 먼저 확인
            String breakerKey = CIRCUIT_BREAKER_PREFIX + normalizedUrl;
            Boolean isBlocked = redisTemplate.hasKey(breakerKey);
            if (Boolean.TRUE.equals(isBlocked)) {
                log.debug("WebSocket 서버 Circuit Breaker 차단 중: {}", serverUrl);
                return false;
            }
            
            // Heartbeat Key 존재 여부로 상태 확인
            String healthKey = HEALTH_KEY_PREFIX + normalizedUrl;
            Boolean exists = redisTemplate.hasKey(healthKey);
            
            if (Boolean.TRUE.equals(exists)) {
                return true;
            }
            
            log.warn("WebSocket 서버 heartbeat 만료: {}", serverUrl);
            return false;
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
            String key = CIRCUIT_BREAKER_PREFIX + normalizeUrl(serverUrl);
            redisTemplate.opsForValue().set(key, "BLOCKED", circuitBreakerTtl, TimeUnit.SECONDS);
            log.warn("WebSocket 서버 Circuit Breaker 활성화: {} ({}초간)", serverUrl, circuitBreakerTtl);
        } catch (Exception e) {
            log.error("Circuit Breaker 설정 실패: {}", serverUrl, e);
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

