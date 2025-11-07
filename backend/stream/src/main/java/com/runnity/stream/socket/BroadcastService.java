package com.runnity.stream.socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastService {

    private final BroadcastHandler broadcastHandler;

    // Kafka 메시지를 수신하면 WebSocket으로 뿌릴 예정 (지금은 로그만 찍기)
    @KafkaListener(topics = "running-data", groupId = "runnity-stream-group")
    public void consume(String message) {
        log.info("📩 Kafka 메시지 수신: {}", message);
        // 나중에 broadcastHandler.sendToAll(message); 로 연결 예정
    }
}
