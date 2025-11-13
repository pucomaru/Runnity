package com.runnity.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.websocket.dto.kafka.KafkaStreamEvent;
import com.runnity.websocket.enums.KafkaEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 챌린지 Kafka 프로듀서
 * 
 * challenge-stream 토픽으로 이벤트를 발행합니다.
 */
@Slf4j
@Service
public class ChallengeKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "challenge-stream";

    public ChallengeKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 챌린지 시작 이벤트 발행
     * 
     * @param challengeId 챌린지 ID
     * @param runnerId 러너 ID (Member PK)
     * @param nickname 닉네임
     * @param profileImage 프로필 이미지 URL
     */
    public void publishStartEvent(Long challengeId, Long runnerId, String nickname, String profileImage) {
        try {
            // isBroadcast 여부 확인
            boolean isBroadcast = isBroadcast(challengeId);
            if (!isBroadcast) {
                log.debug("브로드캐스트가 아닌 챌린지: challengeId={}", challengeId);
                return;
            }

            // KafkaStreamEvent 생성
            KafkaStreamEvent event = new KafkaStreamEvent(
                    KafkaEventType.START,
                    challengeId,
                    runnerId,
                    nickname,
                    profileImage,
                    0.0,  // 초기 distance
                    0.0,  // 초기 pace
                    null, // 초기 ranking (아직 순위 없음)
                    isBroadcast,
                    null, // reason (start는 불필요)
                    System.currentTimeMillis()
            );

            // JSON 직렬화
            String eventJson = objectMapper.writeValueAsString(event);

            // Kafka 발행 (Key: challengeId, Value: eventJson)
            kafkaTemplate.send(TOPIC, challengeId.toString(), eventJson);

            log.info("Kafka START 이벤트 발행: challengeId={}, runnerId={}, nickname={}", 
                    challengeId, runnerId, nickname);
        } catch (Exception e) {
            log.error("Kafka START 이벤트 발행 실패: challengeId={}, runnerId={}", challengeId, runnerId, e);
        }
    }
    
    /**
     * 러닝 기록 이벤트 발행
     * 
     * @param challengeId 챌린지 ID
     * @param runnerId 러너 ID
     * @param nickname 닉네임
     * @param profileImage 프로필 이미지 URL
     * @param distance 현재 거리
     * @param pace 현재 페이스
     * @param ranking 현재 순위
     */
    public void publishRunningEvent(Long challengeId, Long runnerId, String nickname, String profileImage,
                                     Double distance, Double pace, Integer ranking) {
        try {
            boolean isBroadcast = isBroadcast(challengeId);
            if (!isBroadcast) {
                log.debug("브로드캐스트가 아닌 챌린지: challengeId={}", challengeId);
                return;
            }

            KafkaStreamEvent event = new KafkaStreamEvent(
                    KafkaEventType.RUNNING,
                    challengeId,
                    runnerId,
                    nickname,
                    profileImage,
                    distance,
                    pace,
                    ranking,
                    isBroadcast,
                    null,
                    System.currentTimeMillis()
            );

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, challengeId.toString(), eventJson);

            log.debug("Kafka RUNNING 이벤트 발행: challengeId={}, runnerId={}, distance={}", 
                    challengeId, runnerId, distance);
        } catch (Exception e) {
            log.error("Kafka RUNNING 이벤트 발행 실패: challengeId={}, runnerId={}", challengeId, runnerId, e);
        }
    }

    /**
     * 완주 이벤트 발행
     * 
     * @param challengeId 챌린지 ID
     * @param runnerId 러너 ID
     * @param nickname 닉네임
     * @param profileImage 프로필 이미지 URL
     * @param distance 최종 거리
     * @param pace 최종 페이스
     * @param ranking 최종 순위
     */
    public void publishFinishEvent(Long challengeId, Long runnerId, String nickname, String profileImage,
                                    Double distance, Double pace, Integer ranking) {
        try {
            boolean isBroadcast = isBroadcast(challengeId);
            if (!isBroadcast) {
                log.debug("브로드캐스트가 아닌 챌린지: challengeId={}", challengeId);
                return;
            }

            KafkaStreamEvent event = new KafkaStreamEvent(
                    KafkaEventType.FINISH,
                    challengeId,
                    runnerId,
                    nickname,
                    profileImage,
                    distance,
                    pace,
                    ranking,
                    isBroadcast,
                    null,
                    System.currentTimeMillis()
            );

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, challengeId.toString(), eventJson);

            log.info("Kafka FINISH 이벤트 발행: challengeId={}, runnerId={}, distance={}", 
                    challengeId, runnerId, distance);
        } catch (Exception e) {
            log.error("Kafka FINISH 이벤트 발행 실패: challengeId={}, runnerId={}", challengeId, runnerId, e);
        }
    }

    /**
     * 퇴장 이벤트 발행
     * 
     * @param challengeId 챌린지 ID
     * @param runnerId 러너 ID
     * @param nickname 닉네임
     * @param profileImage 프로필 이미지 URL
     * @param distance 현재 거리
     * @param pace 현재 페이스
     * @param ranking 현재 순위
     * @param reason 퇴장 사유
     */
    public void publishLeaveEvent(Long challengeId, Long runnerId, String nickname, String profileImage,
                                   Double distance, Double pace, Integer ranking, String reason) {
        try {
            boolean isBroadcast = isBroadcast(challengeId);
            if (!isBroadcast) {
                log.debug("브로드캐스트가 아닌 챌린지: challengeId={}", challengeId);
                return;
            }

            KafkaStreamEvent event = new KafkaStreamEvent(
                    KafkaEventType.LEAVE,
                    challengeId,
                    runnerId,
                    nickname,
                    profileImage,
                    distance,
                    pace,
                    ranking,
                    isBroadcast,
                    reason,
                    System.currentTimeMillis()
            );

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, challengeId.toString(), eventJson);

            log.info("Kafka LEAVE 이벤트 발행: challengeId={}, runnerId={}, reason={}", 
                    challengeId, runnerId, reason);
        } catch (Exception e) {
            log.error("Kafka LEAVE 이벤트 발행 실패: challengeId={}, runnerId={}, reason={}", 
                    challengeId, runnerId, reason, e);
        }
    }

    /**
     * 브로드캐스트 여부 확인
     * 
     * @param challengeId 챌린지 ID
     * @return 브로드캐스트 여부
     */
    private boolean isBroadcast(Long challengeId) {
        try {
            String key = "broadcast:" + challengeId + ":meta";
            Object flag = redisTemplate.opsForHash().get(key, "isBroadcast");
            
            if (flag == null) {
                log.debug("브로드캐스트 메타 정보 없음: challengeId={}", challengeId);
                return false;
            }
            
            return "true".equalsIgnoreCase(flag.toString());
        } catch (Exception e) {
            log.error("브로드캐스트 여부 확인 실패: challengeId={}", challengeId, e);
            return false;
        }
    }
}

