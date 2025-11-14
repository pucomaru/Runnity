package com.runnity.websocket.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * 비즈니스 서버가 기대하는 Redis Heartbeat 키를 주기적으로 갱신하는 서비스.
 * <p>
 * ws_health:{정규화된서버URL} 형태의 키에 TTL을 부여하여 저장합니다.
 */
@Slf4j
@Service
public class WebSocketHeartbeatService {

    private static final String HEARTBEAT_KEY_PREFIX = "ws_health:";

    private final RedisTemplate<String, String> redisTemplate;
    private final String healthKey;
    private final long heartbeatTtlSeconds;

    public WebSocketHeartbeatService(
            @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate,
            @Value("${websocket.health.server-url}") String serverUrl,
            @Value("${websocket.health.heartbeat-ttl:10}") long heartbeatTtlSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.heartbeatTtlSeconds = heartbeatTtlSeconds;
        this.healthKey = HEARTBEAT_KEY_PREFIX + normalizeUrl(serverUrl);
    }

    @PostConstruct
    public void init() {
        publishHeartbeat();
    }

    @Scheduled(fixedDelayString = "${websocket.health.heartbeat-interval-millis:5000}")
    public void publishHeartbeat() {
        try {
            String timestamp = Instant.now().toString();
            redisTemplate.opsForValue().set(healthKey, timestamp, heartbeatTtlSeconds, TimeUnit.SECONDS);
            log.debug("WebSocket heartbeat 갱신: key={}, ttl={}s", healthKey, heartbeatTtlSeconds);
        } catch (Exception e) {
            log.error("WebSocket heartbeat 갱신 실패: key={}", healthKey, e);
        }
    }

    private String normalizeUrl(String url) {
        return url.replace("ws://", "").replace("wss://", "");
    }
}


