package com.runnity.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.websocket.dto.redis.ParticipantUpdateEvent;
import com.runnity.websocket.dto.redis.UserEnteredEvent;
import com.runnity.websocket.dto.redis.UserLeftEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis Pub/Sub 서비스
 * 
 * 다른 WebSocket 서버들에게 이벤트를 전파합니다.
 * 채널별로 이벤트를 발행하여 서버 간 동기화를 수행합니다.
 */
@Slf4j
@Service
public class RedisPubSubService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHANNEL_ENTER = "challenge:enter";
    private static final String CHANNEL_LEAVE = "challenge:leave";
    private static final String CHANNEL_UPDATE = "challenge:update";

    public RedisPubSubService(
            @Qualifier("pubsubRedisTemplate") RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 사용자 입장 이벤트 발행
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     * @param nickname 닉네임
     * @param profileImage 프로필 이미지 URL
     */
    public void publishUserEntered(Long challengeId, Long userId, String nickname, String profileImage) {
        try {
            UserEnteredEvent event = new UserEnteredEvent(challengeId, userId, nickname, profileImage);
            String message = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(CHANNEL_ENTER, message);
            
            log.info("입장 이벤트 발행: challengeId={}, userId={}, nickname={}", challengeId, userId, nickname);
        } catch (Exception e) {
            log.error("입장 이벤트 발행 실패: challengeId={}, userId={}", challengeId, userId, e);
        }
    }

    /**
     * 사용자 퇴장 이벤트 발행
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     * @param reason 퇴장 사유
     */
    public void publishUserLeft(Long challengeId, Long userId, String reason) {
        try {
            UserLeftEvent event = new UserLeftEvent(challengeId, userId, reason);
            String message = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(CHANNEL_LEAVE, message);
            
            log.info("퇴장 이벤트 발행: challengeId={}, userId={}, reason={}", challengeId, userId, reason);
        } catch (Exception e) {
            log.error("퇴장 이벤트 발행 실패: challengeId={}, userId={}, reason={}", challengeId, userId, reason, e);
        }
    }

    /**
     * 참가자 정보 업데이트 이벤트 발행
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     * @param distance 현재 거리
     * @param pace 현재 페이스
     */
    public void publishParticipantUpdate(Long challengeId, Long userId, Double distance, Integer pace) {
        try {
            ParticipantUpdateEvent event = new ParticipantUpdateEvent(challengeId, userId, distance, pace);
            String message = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(CHANNEL_UPDATE, message);
            
            log.debug("참가자 업데이트 이벤트 발행: challengeId={}, userId={}, distance={}, pace={}", 
                    challengeId, userId, distance, pace);
        } catch (Exception e) {
            log.error("참가자 업데이트 이벤트 발행 실패: challengeId={}, userId={}", challengeId, userId, e);
        }
    }
}

