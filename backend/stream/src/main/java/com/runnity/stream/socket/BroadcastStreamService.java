package com.runnity.stream.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.stream.socket.dto.ChallengeStreamMessage;
import com.runnity.stream.socket.dto.HighlightEventDto;
import com.runnity.stream.socket.dto.LLMCommentaryMessage;
import com.runnity.stream.socket.dto.WebSocketPayloadWrapper;
import com.runnity.stream.socket.util.BroadcastRunnerRedisUtil;
import com.runnity.stream.socket.util.ChallengeMetaRedisUtil;
import com.runnity.stream.socket.util.HighlightDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Kafka → 러너 상태 업데이트 → 하이라이트 감지 → WebSocket 송출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastStreamService {

    private WebSocketPayloadWrapper wrapStreamMessage(ChallengeStreamMessage msg) {
        return WebSocketPayloadWrapper.builder()
                .type("STREAM")
                .subtype(msg.getEventType().toUpperCase())
                .challengeId(msg.getChallengeId())
                .timestamp(System.currentTimeMillis())
                .payload(msg)
                .build();
    }


    private static final String WS_DEST_PREFIX = "/topic/broadcast/";
    private static final String HIGHLIGHT_TOPIC = "highlight-event";

    private final ChallengeMetaRedisUtil challengeMetaRedisUtil;
    private final BroadcastRunnerRedisUtil redisRunnerUtil;
    private final HighlightDetector highlightDetector;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();


    public void handleEvent(ChallengeStreamMessage msg) {

        Long challengeId = msg.getChallengeId();
        Long runnerId = msg.getRunnerId();

        if (challengeId == null || runnerId == null) return;

        // 1) 비즈니스 meta 읽기
        Map<String, String> meta = challengeMetaRedisUtil.getMeta(challengeId);
        if (meta.isEmpty()) {
            log.warn("Meta not found for challengeId={}, skip", challengeId);
            return;
        }

        // distance 는 메타에 저장된 총 거리
        double totalDistance = 0;
        try {
            totalDistance = Double.parseDouble(meta.getOrDefault("distance", "0"));
        } catch (Exception ignored) {}

        // 2) 이전 러너 상태 가져오기
        Map<String, Object> prevRunner = redisRunnerUtil.getRunnerData(challengeId, runnerId);

        Integer newRank = msg.getRanking();
        Long prevRankOwner = (newRank != null)
                ? redisRunnerUtil.getRunnerIdByRank(challengeId, newRank)
                : null;

        // 3) 러너 상태 갱신
        redisRunnerUtil.updateRunnerProgress(
                challengeId, runnerId,
                msg.getNickname(), msg.getProfileImage(),
                msg.getDistance(), msg.getPace(), msg.getRanking()
        );

        if (newRank != null)
            redisRunnerUtil.setRankOwner(challengeId, newRank, runnerId);

        // 4) 하이라이트 감지
        HighlightEventDto highlight = highlightDetector.detect(
                msg, prevRunner, prevRankOwner, totalDistance
        );

        if (highlight != null) {
            publishHighlight(highlight, challengeId, prevRankOwner);
        }

        // 5) WS 전달
        sendToWebSocket(msg);
    }

    public void handleStart(ChallengeStreamMessage msg) {

        Long challengeId = msg.getChallengeId();
        Long runnerId = msg.getRunnerId();

        // 1) runner 정보 Redis 저장 (대기 UI 구성용)
        redisRunnerUtil.updateRunnerProgress(
                challengeId,
                runnerId,
                msg.getNickname(),
                msg.getProfileImage(),
                msg.getDistance(),   // 보통 0
                msg.getPace(),
                msg.getRanking()     // null일 수도 있음
        );

        // 2) 프론트로 WS 브로드캐스트
        var wrapper = wrapStreamMessage(msg);
        messagingTemplate.convertAndSend(WS_DEST_PREFIX + challengeId, wrapper);

        log.info("[START] Runner added to waiting list: {}", msg);
    }

    private void publishHighlight(HighlightEventDto highlight, Long challengeId, Long targetId) {
        try {

            if ("OVERTAKE".equals(highlight.getHighlightType()) && targetId != null) {
                Map<String, Object> target = redisRunnerUtil.getRunnerData(challengeId, targetId);
                if (target != null) {
                    highlight.setTargetNickname((String) target.get("nickname"));
                    highlight.setTargetProfileImage((String) target.get("profileImage"));
                }
            }

            String json = objectMapper.writeValueAsString(highlight);
            kafkaTemplate.send(HIGHLIGHT_TOPIC, json);

            log.info("[HIGHLIGHT] {}", json);

        } catch (JsonProcessingException e) {
            log.error("highlight serialization error: {}", e.getMessage());
        }
    }

    private void sendToWebSocket(ChallengeStreamMessage msg) {
        try {
            var wrapper = wrapStreamMessage(msg);
            messagingTemplate.convertAndSend(WS_DEST_PREFIX + msg.getChallengeId(), wrapper);
        } catch (Exception e) {
            log.error("WS broadcast error: {}", e.getMessage());
        }
    }

    private WebSocketPayloadWrapper wrapLLMMessage(LLMCommentaryMessage llm) {
        return WebSocketPayloadWrapper.builder()
                .type("LLM")
                .subtype(llm.getHighlightType())
                .challengeId(llm.getChallengeId())
                .timestamp(System.currentTimeMillis())
                .payload(llm)
                .build();
    }

    public void sendLLMCommentary(LLMCommentaryMessage llm) {
        var wrapper = wrapLLMMessage(llm);
        messagingTemplate.convertAndSend(WS_DEST_PREFIX + llm.getChallengeId(), wrapper);
    }


}
