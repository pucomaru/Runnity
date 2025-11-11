package com.runnity.stream.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.stream.socket.BroadcastService;
import com.runnity.stream.socket.dto.ChallengeStreamMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BroadcastConsumer {

    private final ObjectMapper objectMapper;
    private final BroadcastService broadcastService;

    @KafkaListener(topics = "challenge-stream", groupId = "stream-broadcast-group")
    public void consume(String message) {
        try {
            ChallengeStreamMessage streamMsg = objectMapper.readValue(message, ChallengeStreamMessage.class);
            broadcastService.handleEvent(streamMsg);
        } catch (Exception e) {
            log.error("Kafka consume error: {}", e.getMessage());
        }
    }
}
