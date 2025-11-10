package com.runnity.stream.socket.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
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

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event){
        log.info("viewer connected");
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event){
        log.info("Viewer disconnected");
    }


}
