package com.runnity.stream.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.stream.socket.dto.BroadcastResponse;
import com.runnity.stream.socket.dto.ChallengeStreamMessage;
import com.runnity.stream.socket.util.BroadcastRedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 중계방송 관련 비즈니스 로직
 * - Redis 방송 메타 관리
 * - Kafka Consumer에서 수신한 이벤트 처리
 * - WebSocket(STOMP)으로 실시간 송출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastSessionService {

    private final BroadcastRedisUtil redisUtil;
    private final SimpMessagingTemplate messagingTemplate;      // STOMP 전송용
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ======== 이벤트별 처리 ========
    private void handleStart(ChallengeStreamMessage msg) {
        Long id = msg.getChallengeId();
        if (Boolean.TRUE.equals(msg.getIsBroadcast())) {
            log.info("챌린지 시작: challengeId={}, runnerId={}", id, msg.getRunnerId());
            redisUtil.updateStatus(id, "LIVE");
        }
        broadcastToViewers(msg);
    }

    private void handleRunning(ChallengeStreamMessage msg) {
        log.debug("실시간 진행중: {}", msg);
        broadcastToViewers(msg);
    }

    private void handleFinish(ChallengeStreamMessage msg) {
        log.info("완주 이벤트: challengeId={}, runnerId={}", msg.getChallengeId(), msg.getRunnerId());
        broadcastToViewers(msg);
    }

    private void handleLeave(ChallengeStreamMessage msg) {
        String reason = msg.getReason();
        log.info("LEAVE event: runnerId={}, reason={}", msg.getRunnerId(), reason);

        // 재접속 가능한 사유인 경우 (TIMEOUT, DISCONNECTED, ERROR)
        if (reason != null && List.of("TIMEOUT", "DISCONNECTED", "ERROR").contains(reason)) {
            log.info("재접속 허용 대상 runnerId={}", msg.getRunnerId());
        }

        broadcastToViewers(msg);
    }

    /**
     * 실제 WebSocket(STOMP) 송출
     */
    private void broadcastToViewers(ChallengeStreamMessage msg) {
        try {
            messagingTemplate.convertAndSend("/topic/broadcast/" + msg.getChallengeId(), msg);
            log.debug("Sent to viewers: {}", msg);
        } catch (Exception e) {
            log.error("Failed to broadcast to viewers: {}", e.getMessage());
        }
    }


    // Kafka 이벤트 처리 (ChallengeStreamMessage)
    public void handleEvent(ChallengeStreamMessage msg) throws JsonProcessingException {
        Long challengeId = msg.getChallengeId();
        String event = msg.getEventType();

        // --- 방송 세션 메타 확인 ---
        if (!redisUtil.existsMeta(challengeId)) {
            log.warn("Redis 메타 없음, failover 시도: challengeId={}", challengeId);
            // TODO: MySQL에서 challengeId 기반 메타 조회 후 redisUtil.createMetaSession() 호출
        }

        switch (event.toLowerCase()) {
            case "start" -> handleStart(msg);
            case "running" -> handleRunning(msg);
            case "finish" -> handleFinish(msg);
            case "leave" -> handleLeave(msg);
            default -> log.warn("Unknown eventType: {}", event);
        }
    }

    // 챌린지 시작 시 방송 세션 생성
    public void createBroadcastSession (Long challengeId, String title,int participantCount){
        redisUtil.createSession(challengeId, title, participantCount);
    }

    // 방송 상태 변경(WAITING, LIVE, ENDED)
    public void updateStatus (Long challengeId, String status){
        redisUtil.updateStatus(challengeId, status);
    }

    // 시청자 입장시 +1
    public void addViewer (Long challengeId){
        redisUtil.increaseViewer(challengeId);
    }

    // 시청자 퇴장시 -1
    public void removeViewer (Long challengeId){
        redisUtil.decreaseViewer(challengeId);
    }

    // 방송 종료 시 세션 제거
    public void endBroadcast (Long challengeId){
        redisUtil.expireSession(challengeId);
    }

    // 현재 활성 방송 목록 조회
    public List<BroadcastResponse> getActiveBroadcasts () {
        List<Map<Object, Object>> rawList = redisUtil.getActiveSessions();
        List<BroadcastResponse> result = new ArrayList<>();

        for (Map<Object, Object> map : rawList) {
            try {
                Long challengeId = Long.parseLong(String.valueOf(map.get("challengeId")));
                String title = String.valueOf(map.get("title"));
                Integer viewerCount = Integer.parseInt(String.valueOf(map.get("viewerCount")));
                Integer participantCount = Integer.parseInt(String.valueOf(map.get("participantCount")));
                String createdAt = String.valueOf(map.get("createdAt"));

                result.add(new BroadcastResponse(challengeId, title, viewerCount, participantCount, createdAt));
            } catch (Exception e) {
                log.error("Failed to parse broadcast info: {}", e.getMessage());
            }
        }

        return result;
    }
}
