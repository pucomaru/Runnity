package com.runnity.websocket.config;

import com.runnity.websocket.handler.ChallengeWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChallengeWebSocketHandler challengeWebSocketHandler;

    @Value("${websocket.allowed-origins}")
    private String allowedOriginsRaw;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] origins = allowedOriginsRaw.split(",");

        registry.addHandler(challengeWebSocketHandler, "/ws/challenge/{challengeId}")
                .setAllowedOrigins(origins)
                .setAllowedOriginPatterns(origins);
    }
}
