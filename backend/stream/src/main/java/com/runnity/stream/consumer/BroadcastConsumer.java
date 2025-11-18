package com.runnity.stream.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.stream.socket.BroadcastSessionService;
import com.runnity.stream.socket.BroadcastSessionService;
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
    private final BroadcastSessionService broadcastSessionService;

    @KafkaListener(topics = "challenge-stream", groupId = "stream-broadcast-group")
    public void consume(String message) {
        try {
            ChallengeStreamMessage streamMsg = objectMapper.readValue(message, ChallengeStreamMessage.class);

            log.debug("Kafka message consumed: {}", streamMsg);
            broadcastSessionService.handleEvent(streamMsg);
        } catch (Exception e) {
            log.error("Kafka consume error: raw = {} | err = {}", message, e.getMessage());
        }
    }
}
