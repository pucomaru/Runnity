package com.runnity.global.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * WebSocket 서버 URL 설정
 * application.yml에서 websocket.servers 리스트를 주입받음
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "websocket")
@Getter
@Setter
public class WebSocketServerConfig {
    
    /**
     * WebSocket 서버 URL 리스트
     * 예: ["ws://ws-server-1.runnity.com", "ws://ws-server-2.runnity.com"]
     */
    private List<String> servers;

    /**
     * 초기화 시 WebSocket 서버 설정 검증
     */
    @PostConstruct
    public void validate() {
        if (servers == null || servers.isEmpty()) {
            log.error("WebSocket 서버 설정이 없습니다. application.yml에 websocket.servers를 설정하세요.");
            throw new IllegalStateException(
                    "WebSocket 서버가 설정되지 않았습니다. application.yml에 websocket.servers를 추가하세요."
            );
        }
        log.info("WebSocket 서버 설정 완료: {} 개의 서버", servers.size());
        for (int i = 0; i < servers.size(); i++) {
            log.info("  - 서버 #{}: {}", i + 1, servers.get(i));
        }
    }

    /**
     * WebSocket 서버 개수
     */
    public int getServerCount() {
        return servers != null ? servers.size() : 0;
    }

    /**
     * 인덱스로 WebSocket 서버 URL 조회
     * 
     * @param index 서버 인덱스
     * @return WebSocket 서버 URL
     * @throws IllegalArgumentException 유효하지 않은 인덱스인 경우
     */
    public String getServerUrl(int index) {
        if (servers == null || index < 0 || index >= servers.size()) {
            throw new IllegalArgumentException("Invalid server index: " + index);
        }
        return servers.get(index);
    }
}

