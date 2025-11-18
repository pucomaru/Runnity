package com.runnity.stream.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.stream.socket.BroadcastStateService;
import com.runnity.stream.socket.BroadcastStreamService;
import com.runnity.stream.socket.dto.ChallengeStreamMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
/*
 * challenge-stream 토픽 consumer
 * - 문자열 JSON → ChallengeStreamMessage 매핑 후 BroadcastService 로 위임
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BroadcastConsumer {

    private final ObjectMapper objectMapper;
    private final BroadcastStreamService broadcastStreamService;
    private final BroadcastStateService broadcastStateService;

    @KafkaListener(topics = "challenge-stream", groupId = "stream-broadcast-group")
    public void consume(String message) {
        try {
            ChallengeStreamMessage msg = objectMapper.readValue(message, ChallengeStreamMessage.class);
            log.debug("Kafka message consumed: {}", msg);

            String event = msg.getEventType();
            if (event == null) {
                log.warn("EventType null: {}", msg);
                return;
            }

            switch (event.toLowerCase()) {

                // === 1) 방송 상태 이벤트 ===
                case "ready" -> broadcastStateService.handleReady(msg.getChallengeId());
                case "running" -> broadcastStateService.handleRunning(msg.getChallengeId());
                case "done" -> broadcastStateService.handleDone(msg.getChallengeId());

                // === 2) 실시간 상태 / 러너 이벤트 ===
                case "start", "finish", "leave" -> broadcastStreamService.handleEvent(msg);

                // === 3) 일반 running 프레임 (주의: READY/RUNNING 구분 필요)
                default -> broadcastStreamService.handleEvent(msg);
            }

        } catch (Exception e) {
            log.error("Kafka consume error: raw = {} | err = {}", message, e.getMessage());
        }
    }
}
