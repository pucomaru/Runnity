package com.runnity.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * WebSocket 서버 URL 설정
 * application.yml에서 websocket.servers 리스트를 주입받음
 */
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

