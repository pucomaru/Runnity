package com.runnity.stream.socket.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket 연결/해제 이벤트를 감지하는 리스너 클래스

- STOMP 프로토콜 기반으로 동작
- 연결 시 viewerCount +1
- 해제 시 viewerCount -1

 Spring 내부적으로 WebSocket 세션 연결 시
 SessionConnectEvent / SessionDisconnectEvent 이벤트가 자동 발생
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ViewerCountService viewerCountService;

    // 관전자기 websocket에 연결됐을 때 호출
    @EventListener
    public void handleSessionConnect(SessionConnectEvent event){
        try{
            
            // stomp 헤더 정보 추출
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            String challengeId = accessor.getFirstNativeHeader("challengeId"); // 프론트에서 헤더로 전달

            if(challengeId != null){
                viewerCountService.increment(Long.parseLong(challengeId));
                log.info("Viewer connected | challengeId = {} | sessionId = {}",challengeId, accessor.getSessionId());
            }else{
                log.warn("Connected without challengeId header. session={}", accessor.getSessionId());
            }

        }catch (Exception e){
            log.error("error on websocket connect event :{}", e.getMessage());
        }

    }

    // 관전자가 websocket 연결 종료했을 떄 호출
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event){
        try{
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
            String challengeId = accessor.getFirstNativeHeader("challengeId");

            if (challengeId != null) {
                viewerCountService.decrement(Long.parseLong(challengeId));
                log.info("Viewer disconnected | challengeId={} | session={}", challengeId, accessor.getSessionId());
            } else {
                log.warn("Disconnected without challengeId header. session={}", accessor.getSessionId());
            }

        }catch (Exception e){
            log.error("Error on WebSocket disconnect event: {}", e.getMessage());
        }
    }


}
