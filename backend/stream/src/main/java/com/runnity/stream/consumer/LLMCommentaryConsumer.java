package com.runnity.stream.consumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.stream.socket.BroadcastStreamService;
import com.runnity.stream.socket.dto.LLMCommentaryMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LLMCommentaryConsumer {

    private final BroadcastStreamService streamService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // LLM 서버가 만든 하이라이트 멘트가 들어오는 토픽
    private static final String TOPIC = "highlight-llm-commentary";

    @KafkaListener(topics = TOPIC, groupId = "broadcast-llm-group")
    public void consume(String json) {
        try {
            LLMCommentaryMessage msg = objectMapper.readValue(json, LLMCommentaryMessage.class);

            log.info("[LLM COMMENTARY RECEIVED] {}", msg);

            // WS 에 송출
            streamService.sendLLMCommentary(msg);

        } catch (Exception e) {
            log.error("Failed to process LLM commentary message: {}", e.getMessage());
        }
    }
}