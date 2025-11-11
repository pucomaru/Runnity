package com.runnity.stream.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.stream.socket.dto.ChallengeStreamMessage;
import com.runnity.stream.socket.dto.HighlightEventDto;
import com.runnity.stream.socket.util.BroadcastRedisUtil;
import com.runnity.stream.socket.util.BroadcastRunnerRedisUtil;
import com.runnity.stream.socket.util.HighlightDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Kafka 이벤트 → Redis 러너 상태 업데이트 → 하이라이트 감지 → WebSocket 브로드캐스트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastStreamService {

    private static final String WS_DEST_PREFIX = "/topic/broadcast/";
    private static final String HIGHLIGHT_TOPIC = "highlight-event";

    private final BroadcastRedisUtil redisMetaUtil;
    private final BroadcastRunnerRedisUtil redisRunnerUtil;
    private final HighlightDetector highlightDetector;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handleEvent(ChallengeStreamMessage msg) {
        Long challengeId = msg.getChallengeId();
        Long runnerId = msg.getRunnerId();
        if (challengeId == null || runnerId == null) return;

        // 1. 메타 세션 보장
        ensureMeta(challengeId, msg);

        // 2️. 이전 상태 조회
        Map<String, Object> prevRunner = redisRunnerUtil.getRunnerData(challengeId, runnerId);
        Integer newRank = msg.getRanking();
        Long prevRankOwner = (newRank != null)
                ? redisRunnerUtil.getRunnerIdByRank(challengeId, newRank)
                : null;

        // 3. 현재 상태 갱신
        redisRunnerUtil.updateRunnerProgress(challengeId, runnerId,
                msg.getNickname(), msg.getProfileImage(),
                msg.getDistance(), msg.getPace(), msg.getRanking());

        if (newRank != null)
            redisRunnerUtil.setRankOwner(challengeId, newRank, runnerId);

        // 4️. 하이라이트 감지
        double totalDistance = redisMetaUtil.getTotalDistance(challengeId);
        HighlightEventDto highlight = highlightDetector.detect(msg, prevRunner, prevRankOwner, totalDistance);

        if (highlight != null) {
            publishHighlight(highlight, challengeId, prevRankOwner);
        }

        // 5️. WebSocket 브로드캐스트
        sendToWebSocket(msg);
    }

    private void ensureMeta(Long challengeId, ChallengeStreamMessage msg) {
        if (redisMetaUtil.existsMeta(challengeId)) return;
        redisMetaUtil.createMetaSession(challengeId, "Challenge " + challengeId, 0,
                msg.getDistance() != null ? msg.getDistance() : 0.0,
                msg.getIsBroadcast() != null && msg.getIsBroadcast());
        log.info("Meta created for challengeId={}", challengeId);
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
            log.info("[HIGHLIGHT EVENT] {}", json);
        } catch (JsonProcessingException e) {
            log.error("Highlight serialize error: {}", e.getMessage());
        }
    }

    private void sendToWebSocket(ChallengeStreamMessage msg) {
        try {
            messagingTemplate.convertAndSend(WS_DEST_PREFIX + msg.getChallengeId(), msg);
        } catch (Exception e) {
            log.error("WS broadcast error: {}", e.getMessage());
        }
    }
}
