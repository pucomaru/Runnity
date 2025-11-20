//package com.runnity.stream.socket;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.runnity.stream.socket.dto.BroadcastResponse;
//import com.runnity.stream.socket.dto.ChallengeStreamMessage;
//import com.runnity.stream.socket.util.BroadcastRedisUtil;
//import com.runnity.stream.socket.util.ChallengeMetaRedisUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * 중계방송 관련 비즈니스 로직
// * - Redis 방송 메타 관리
// * - Kafka Consumer에서 수신한 이벤트 처리
// * - WebSocket(STOMP)으로 실시간 송출
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class BroadcastSessionService {
//
//    private final BroadcastRedisUtil redisUtil;
//    private final ChallengeMetaRedisUtil challengeMetaRedisUtil;
//    private final SimpMessagingTemplate messagingTemplate;      // STOMP 전송용
//
//    // ======== 이벤트별 처리 ========
//
//    private void handleReady(ChallengeStreamMessage msg) {
//        Long id = msg.getChallengeId();
//        if (id == null) return;
//
//        // 챌린지팀 설계상: ready 는 "중계 대기방 생성" 의미
//        // 여기서 active 목록에 등록
//        redisUtil.addActive(id);
//        log.info("[READY] 방송 활성화: challengeId={}", id);
//
//        // 굳이 WebSocket으로 내릴 필요 없으면 생략 가능
//        // broadcastToViewers(msg);
//    }
//
//
//    private void handleStart(ChallengeStreamMessage msg) {
////        Long id = msg.getChallengeId();
////        if (Boolean.TRUE.equals(msg.getIsBroadcast())) {
////            log.info("챌린지 시작: challengeId={}, runnerId={}", id, msg.getRunnerId());
////            redisUtil.updateStatus(id, "LIVE");
////        }
////        broadcastToViewers(msg);
//    }
//
//    private void handleRunning(ChallengeStreamMessage msg) {
//        log.debug("실시간 진행중: {}", msg);
////        broadcastToViewers(msg);
//    }
//
//    private void handleFinish(ChallengeStreamMessage msg) {
//        log.info("완주 이벤트: challengeId={}, runnerId={}", msg.getChallengeId(), msg.getRunnerId());
////        broadcastToViewers(msg);
//    }
//
//    private void handleLeave(ChallengeStreamMessage msg) {
//        String reason = msg.getReason();
//        log.info("LEAVE event: runnerId={}, reason={}", msg.getRunnerId(), reason);
//
//        // 재접속 가능한 사유인 경우 (TIMEOUT, DISCONNECTED, ERROR)
//        if (reason != null && List.of("TIMEOUT", "DISCONNECTED", "ERROR").contains(reason)) {
//            log.info("재접속 허용 대상 runnerId={}", msg.getRunnerId());
//        }
//
////        broadcastToViewers(msg);
//    }
//
//    private void handleDone(ChallengeStreamMessage msg) {
//        Long id = msg.getChallengeId();
//        if (id == null) return;
//
//        // 챌린지 종료 → active 목록에서 제거
//        redisUtil.removeActive(id);
//        log.info("[DONE] 방송 종료: challengeId={}", id);
//
//        // 필요하다면 마지막 종료 이벤트를 관전자에게도 보낼 수 있음
//        // broadcastToViewers(msg);
//    }
//
////    /**
////     * 실제 WebSocket(STOMP) 송출
////     */
////    private void broadcastToViewers(ChallengeStreamMessage msg) {
////        try {
////            messagingTemplate.convertAndSend("/topic/broadcast/" + msg.getChallengeId(), msg);
////            log.debug("Sent to viewers: {}", msg);
////        } catch (Exception e) {
////            log.error("Failed to broadcast to viewers: {}", e.getMessage());
////        }
////    }
//
//
//    // Kafka 이벤트 처리 (ChallengeStreamMessage)
//    public void handleEvent(ChallengeStreamMessage msg) throws JsonProcessingException {
//        Long challengeId = msg.getChallengeId();
//        String event = msg.getEventType();
//
//        if (challengeId == null || event == null) {
//            log.warn("Invalid message: {}", msg);
//            return;
//        }
//
//        switch (event.toLowerCase()) {
//            case "ready" -> handleReady(msg);
//            case "start" -> handleStart(msg);
//            case "running" -> handleRunning(msg);
//            case "finish" -> handleFinish(msg);
//            case "leave" -> handleLeave(msg);
//            case "done" -> handleDone(msg);
//            default -> log.warn("Unknown eventType: {}", event);
//        }
//    }
//
//    // 시청자 입장시 +1
//    public void addViewer (Long challengeId){
//        redisUtil.increaseViewer(challengeId);
//    }
//
//    // 시청자 퇴장시 -1
//    public void removeViewer (Long challengeId){
//        redisUtil.decreaseViewer(challengeId);
//    }
//
//    // 현재 활성 방송 목록 조회
//    public List<BroadcastResponse> getActiveBroadcasts (
//            String keyword,
//            List<String> distanceCodes,
//            String sort
//    ) {
//        // 1) 활성 챌린지 ID 목록 가져오기
//        var ids = redisUtil.getActiveChallengeIds();
//        List<BroadcastResponse> result = new ArrayList<>();
//
//        for (String idStr : ids) {
//            try {
//                Long challengeId = Long.parseLong(idStr);
//
//                // 2) 비즈니스 meta 읽기
//                Map<String, String> meta = challengeMetaRedisUtil.getMeta(challengeId);
//                if (meta.isEmpty()) {
//                    log.warn("Meta not found for challengeId={}", challengeId);
//                    continue;
//                }
//
//                String title = meta.getOrDefault("title", "제목 없음");
//                String distanceRaw = meta.getOrDefault("distance", "UNKNOWN");
//                String distanceCode = normalizeDistance(distanceRaw);
//
//                int participantCount = 0;
//                try {
//                    participantCount = Integer.parseInt(
//                            meta.getOrDefault("actualParticipantCount", "0")
//                    );
//                } catch (NumberFormatException e) {
//                    log.warn("Invalid participantCount for challengeId={}", challengeId);
//                }
//
//                // 3) viewerCount & createdAt 은 stream Redis에서
//                int viewerCount = (int) redisUtil.getViewerCount(challengeId);
//                String createdAt = redisUtil.getCreatedAt(challengeId);
//
//                result.add(new BroadcastResponse(
//                        challengeId,
//                        title,
//                        viewerCount,
//                        participantCount,
//                        createdAt,
//                        distanceCode
//                ));
//            } catch (Exception e) {
//                log.error("Failed to build broadcast response: {}", e.getMessage());
//            }
//        }
//
//        // ===== 기존 필터/정렬 로직 재사용 =====
//
//        // 1) 키워드(제목) 필터
//        if (keyword != null && !keyword.isBlank()) {
//            String kw = keyword.toLowerCase();
//            result = result.stream()
//                    .filter(b -> b.getTitle() != null && b.getTitle().toLowerCase().contains(kw))
//                    .toList();
//        }
//
//        // 2) 거리 필터(Optional)
//        if (distanceCodes != null && !distanceCodes.isEmpty()) {
//            result = result.stream()
//                    .filter(b -> b.getDistance() != null && distanceCodes.contains(b.getDistance()))
//                    .toList();
//        }
//
//        // 3) 정렬
//        if ("POPULAR".equalsIgnoreCase(sort)) {
//            result = result.stream()
//                    .sorted((a, b) -> Integer.compare(b.getViewerCount(), a.getViewerCount()))
//                    .toList();
//        } else { // LATEST(default): createdAt 역순
//            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
//            result = result.stream()
//                    .sorted((a, b) -> {
//                        try {
//                            var timeA = LocalDateTime.parse(a.getCreatedAt(), fmt);
//                            var timeB = LocalDateTime.parse(b.getCreatedAt(), fmt);
//                            return timeB.compareTo(timeA); // 최신순
//                        } catch (Exception e) {
//                            return 0;
//                        }
//                    })
//                    .toList();
//        }
//
//        return result;
//    }
//
//    private String normalizeDistance(String raw) {
//        if (raw == null) return "UNKNOWN";
//        // 이미 코드로 저장된 경우 그대로 통과
//        switch (raw) {
//            case "M100": case "M500":
//            case "ONE": case "TWO": case "THREE": case "FOUR": case "FIVE":
//            case "SIX": case "SEVEN": case "EIGHT": case "NINE": case "TEN":
//            case "FIFTEEN": case "HALF":
//                return raw;
//        }
//        // 숫자 문자열 → 코드 매핑
//        return switch (raw) {
//            case "0.1" -> "M100";
//            case "0.5" -> "M500";
//            case "1" -> "ONE";
//            case "2" -> "TWO";
//            case "3" -> "THREE";
//            case "4" -> "FOUR";
//            case "5" -> "FIVE";
//            case "6" -> "SIX";
//            case "7" -> "SEVEN";
//            case "8" -> "EIGHT";
//            case "9" -> "NINE";
//            case "10" -> "TEN";
//            case "15" -> "FIFTEEN";
//            default -> "UNKNOWN";
//        };
//    }
//}
