package com.runnity.websocket.service;

import com.runnity.websocket.enums.LeaveReason;
import com.runnity.websocket.manager.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.Set;

/**
 * 타임아웃 체크 서비스
 * 
 * 일정 시간 동안 RECORD 메시지가 없는 참가자를 자동으로 퇴장 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeoutCheckService {

    private static final String LAST_RECORD_TIME_PREFIX = "challenge:%d:user:%d:lastRecord";
    private static final long TIMEOUT_SECONDS = 60; // 60초 동안 RECORD 없으면 타임아웃

    @Qualifier("stringRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;
    private final SessionManager sessionManager;
    private final ChallengeParticipationService participationService;
    private final RedisPubSubService redisPubSubService;
    private final ChallengeKafkaProducer kafkaProducer;

    /**
     * 마지막 RECORD 시간 업데이트
     */
    public void updateLastRecordTime(Long challengeId, Long userId) {
        try {
            String key = String.format(LAST_RECORD_TIME_PREFIX, challengeId, userId);
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            redisTemplate.opsForValue().set(key, timestamp);
            log.debug("마지막 RECORD 시간 업데이트: challengeId={}, userId={}, timestamp={}", 
                    challengeId, userId, timestamp);
        } catch (Exception e) {
            log.error("마지막 RECORD 시간 업데이트 실패: challengeId={}, userId={}", challengeId, userId, e);
        }
    }

    /**
     * 마지막 RECORD 시간 제거
     */
    public void removeLastRecordTime(Long challengeId, Long userId) {
        try {
            String key = String.format(LAST_RECORD_TIME_PREFIX, challengeId, userId);
            redisTemplate.delete(key);
            log.debug("마지막 RECORD 시간 제거: challengeId={}, userId={}", challengeId, userId);
        } catch (Exception e) {
            log.error("마지막 RECORD 시간 제거 실패: challengeId={}, userId={}", challengeId, userId, e);
        }
    }

    /**
     * 타임아웃 체크 (30초마다 실행)
     * 
     * 모든 활성 참가자의 마지막 RECORD 시간을 체크하여 타임아웃된 참가자를 처리합니다.
     */
    @Scheduled(fixedRate = 30000) // 30초마다 체크
    public void checkTimeouts() {
        try {
            long currentTime = Instant.now().getEpochSecond();
            long timeoutThreshold = currentTime - TIMEOUT_SECONDS;

            log.debug("타임아웃 체크 시작: currentTime={}, timeoutThreshold={}", currentTime, timeoutThreshold);

            // Redis에서 challenge:*:user:*:lastRecord 패턴의 모든 키를 스캔
            String pattern = "challenge:*:user:*:lastRecord";
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys == null || keys.isEmpty()) {
                log.debug("타임아웃 체크 대상 없음");
                return;
            }

            log.debug("타임아웃 체크 대상: {}명", keys.size());

            for (String key : keys) {
                try {
                    // 키에서 challengeId와 userId 추출
                    // 형식: challenge:{challengeId}:user:{userId}:lastRecord
                    String[] parts = key.split(":");
                    if (parts.length != 5) {
                        log.warn("잘못된 키 형식: {}", key);
                        continue;
                    }
                    
                    Long challengeId = Long.parseLong(parts[1]);
                    Long userId = Long.parseLong(parts[3]);
                    
                    // 마지막 RECORD 시간 조회
                    String lastRecordTimeStr = redisTemplate.opsForValue().get(key);
                    
                    if (lastRecordTimeStr == null) {
                        continue; // 이미 삭제되었거나 없음
                    }
                    
                    long lastRecordTime = Long.parseLong(lastRecordTimeStr);
                    
                    // 타임아웃 체크
                    if (lastRecordTime < timeoutThreshold) {
                        log.info("타임아웃 감지: challengeId={}, userId={}, lastRecordTime={}, timeoutThreshold={}", 
                                challengeId, userId, lastRecordTime, timeoutThreshold);
                        handleTimeout(challengeId, userId);
                    }
                } catch (Exception e) {
                    log.error("참가자 타임아웃 체크 실패: key={}", key, e);
                }
            }
            
            log.debug("타임아웃 체크 완료");
            
        } catch (Exception e) {
            log.error("타임아웃 체크 실패", e);
        }
    }
    
    /**
     * 개별 참가자 타임아웃 처리
     */
    private void handleTimeout(Long challengeId, Long userId) {
        try {
            log.info("타임아웃 처리: challengeId={}, userId={}", challengeId, userId);

            // 세션 조회
            WebSocketSession session = sessionManager.getSession(challengeId, userId);
            
            // 참가자 정보 조회
            SessionManager.ParticipantInfo info = sessionManager.getParticipantInfo(challengeId, userId);
            Double distance = info != null ? info.distance() : 0.0;
            Integer pace = info != null ? info.pace() : 0;
            Integer ranking = sessionManager.calculateRanking(challengeId, userId);

            String nickname = session != null ? 
                    (String) session.getAttributes().get("nickname") : null;
            String profileImage = session != null ? 
                    (String) session.getAttributes().get("profileImage") : null;

            if (nickname == null) {
                nickname = "User_" + userId;
            }

            // 1. 참가자 상태 DB 업데이트 (TIMEOUT)
            participationService.updateParticipationStatus(challengeId, userId, LeaveReason.TIMEOUT.getValue());

            // 2. 마지막 RECORD 시간 제거
            removeLastRecordTime(challengeId, userId);

            // 3. 세션 제거
            sessionManager.removeSession(challengeId, userId);

            // 4. Redis Pub/Sub으로 퇴장 이벤트 발행
            redisPubSubService.publishUserLeft(challengeId, userId, LeaveReason.TIMEOUT.getValue());

            // 5. Kafka로 LEAVE 이벤트 발행
            kafkaProducer.publishLeaveEvent(challengeId, userId, nickname, profileImage, 
                    distance, pace, ranking, LeaveReason.TIMEOUT.getValue());

            // 6. 세션이 있으면 연결 종료
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (Exception e) {
                    log.error("세션 종료 실패: challengeId={}, userId={}", challengeId, userId, e);
                }
            }

            log.info("타임아웃 처리 완료: challengeId={}, userId={}", challengeId, userId);

        } catch (Exception e) {
            log.error("타임아웃 처리 실패: challengeId={}, userId={}", challengeId, userId, e);
        }
    }
}

