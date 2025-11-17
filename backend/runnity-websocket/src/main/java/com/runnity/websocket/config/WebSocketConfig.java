package com.runnity.websocket.config;

import com.runnity.websocket.handler.ChallengeWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
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

        registry.addHandler(challengeWebSocketHandler, "/ws")
                .setAllowedOrigins(origins)
                .setAllowedOriginPatterns(origins)
                .addInterceptors(new TicketHandshakeInterceptor());
        
        registry.addHandler(challengeWebSocketHandler, "/ws/**")
                .setAllowedOrigins(origins)
                .setAllowedOriginPatterns(origins)
                .addInterceptors(new TicketHandshakeInterceptor());
    }

    /**
     * WebSocket Handshake 시 티켓을 쿼리 파라미터에서 추출하여 세션 attributes에 저장
     */
    @Slf4j
    static class TicketHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                        WebSocketHandler wsHandler, Map<String, Object> attributes) {
            try {
                // 쿼리 파라미터에서 티켓 추출
                String query = request.getURI().getQuery();
                if (query != null) {
                    String ticket = UriComponentsBuilder.fromUriString("?" + query)
                            .build()
                            .getQueryParams()
                            .getFirst("ticket");
                    
                    if (ticket != null && !ticket.isBlank()) {
                        attributes.put("ticket", ticket);
                        log.debug("티켓 추출 성공: ticket={}", ticket);
                        return true;
                    }
                }
                
                log.warn("티켓 없음: URI={}", request.getURI());
                return false;
            } catch (Exception e) {
                log.error("Handshake 실패", e);
                return false;
            }
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Exception exception) {
            if (exception != null) {
                log.error("Handshake 이후 오류 발생", exception);
            }
        }
    }
}
