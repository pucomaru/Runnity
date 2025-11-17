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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public void createBroadcastSession (Long challengeId, String title,int participantCount, String distanceCode){
        redisUtil.createSession(challengeId, title, participantCount, distanceCode);
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
    public List<BroadcastResponse> getActiveBroadcasts (
            String keyword,
            List<String> distanceCodes,
            String sort
    ) {
        List<Map<String, String>> rawList = redisUtil.getActiveSessions();
        List<BroadcastResponse> result = new ArrayList<>();

        for (Map<String, String> map : rawList) {
            try {
                Long challengeId = Long.parseLong(String.valueOf(map.get("challengeId")));
                String title = String.valueOf(map.get("title"));
                Integer viewerCount = Integer.parseInt(String.valueOf(map.get("viewerCount")));
                Integer participantCount = Integer.parseInt(String.valueOf(map.get("participantCount")));
                String createdAt = String.valueOf(map.get("createdAt"));
                String distance = map.get("distance");
                String distanceCode = normalizeDistance(distance);

                result.add(new BroadcastResponse(challengeId, title, viewerCount, participantCount, createdAt, distanceCode));
            } catch (Exception e) {
                log.error("Failed to parse broadcast info: {}", e.getMessage());
            }
        }

        // 1) 키워드(제목) 필터
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            result = result.stream()
                    .filter(b -> b.getTitle() != null && b.getTitle().toLowerCase().contains(kw))
                    .toList();
        }

        // 2) 거리 필터(Optional)
        if (distanceCodes != null && !distanceCodes.isEmpty()) {
            result = result.stream()
                    .filter(b -> b.getDistance() != null && distanceCodes.contains(b.getDistance()))
                    .toList();
        }

        // 3) 정렬
        if ("POPULAR".equalsIgnoreCase(sort)) {
            result = result.stream()
                    .sorted((a, b) -> Integer.compare(b.getViewerCount(), a.getViewerCount()))
                    .toList();
        } else { // LATEST(default): createdAt 역순
            result = result.stream()
                    .sorted((a, b) -> {
                        try {
                            LocalDateTime timeA = LocalDateTime.parse(a.getCreatedAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            LocalDateTime timeB = LocalDateTime.parse(b.getCreatedAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            return timeA.compareTo(timeB);
                        } catch (Exception e) {
                            log.warn("날짜 파싱 오류: a={}, b={}", a.getCreatedAt(), b.getCreatedAt(), e);
                            // 파싱 실패 시 순서를 바꾸지 않음
                            return 0;
                        }
                    })
                    .toList();
        }

        return result;
    }

    private String normalizeDistance(String raw) {
        if (raw == null) return "UNKNOWN";
        // 이미 코드로 저장된 경우 그대로 통과
        switch (raw) {
            case "ONE": case "TWO": case "THREE": case "FOUR": case "FIVE":
            case "SIX": case "SEVEN": case "EIGHT": case "NINE": case "TEN":
            case "FIFTEEN": case "HALF":
                return raw;
        }
        // 숫자 문자열 → 코드 매핑
        return switch (raw) {
            case "1" -> "ONE";
            case "2" -> "TWO";
            case "3" -> "THREE";
            case "4" -> "FOUR";
            case "5" -> "FIVE";
            case "6" -> "SIX";
            case "7" -> "SEVEN";
            case "8" -> "EIGHT";
            case "9" -> "NINE";
            case "10" -> "TEN";
            case "15" -> "FIFTEEN";
            default -> "HALF";
        };
    }
}
