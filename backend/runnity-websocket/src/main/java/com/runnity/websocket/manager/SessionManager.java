package com.runnity.websocket.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.websocket.dto.websocket.server.ConnectedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 세션 관리자
 * 
 * - 메모리에 세션 객체 저장 (빠른 액세스)
 * - Redis에 참가자 목록 및 정보 저장 (영구성 및 다중 서버 공유)
 */
@Slf4j
@Component
public class SessionManager {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // 메모리 세션 저장소: challengeId:userId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private static final String PARTICIPANT_KEY_PREFIX = "challenge:%d:participants";
    private static final String PARTICIPANT_INFO_PREFIX = "challenge:%d:participant:%d";

    public SessionManager(
            @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 세션 등록 (메모리 + Redis)
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     * @param nickname 닉네임
     * @param profileImage 프로필 이미지 URL
     * @param session WebSocket 세션
     */
    public void registerSession(Long challengeId, Long userId, String nickname, String profileImage, WebSocketSession session) {
        registerSession(challengeId, userId, nickname, profileImage, session, null, null);
    }

    /**
     * 세션 등록 (메모리 + Redis) - 재접속 시 이전 정보 유지
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     * @param nickname 닉네임
     * @param profileImage 프로필 이미지 URL
     * @param session WebSocket 세션
     * @param previousDistance 재접속인 경우 이전 거리 (null이면 0.0)
     * @param previousPace 재접속인 경우 이전 페이스 (null이면 0)
     */
    public void registerSession(Long challengeId, Long userId, String nickname, String profileImage, 
                               WebSocketSession session, Double previousDistance, Integer previousPace) {
        String sessionKey = makeSessionKey(challengeId, userId);
        
        // 1. 메모리에 세션 저장
        sessions.put(sessionKey, session);
        
        // 2. Redis에 참가자 등록 (ZADD - score는 현재 시간)
        String participantsKey = String.format(PARTICIPANT_KEY_PREFIX, challengeId);
        long now = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(participantsKey, userId.toString(), now);
        
        // 3. Redis에 참가자 정보 저장
        // 재접속인 경우 이전 distance, pace 유지, 아니면 0.0 / 0
        String infoKey = String.format(PARTICIPANT_INFO_PREFIX, challengeId, userId);
        try {
            Double distance = previousDistance != null ? previousDistance : 0.0;
            Integer pace = previousPace != null ? previousPace : 0;
            ParticipantInfo info = new ParticipantInfo(userId, nickname, profileImage, distance, pace);
            String infoJson = objectMapper.writeValueAsString(info);
            redisTemplate.opsForValue().set(infoKey, infoJson);
        } catch (JsonProcessingException e) {
            log.error("참가자 정보 저장 실패: challengeId={}, userId={}", challengeId, userId, e);
        }
        
        log.info("세션 등록 완료: challengeId={}, userId={}, nickname={}, sessionId={}, isReconnect={}", 
                challengeId, userId, nickname, session.getId(), previousDistance != null);
    }

    /**
     * 세션 제거 (메모리 + Redis)
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     */
    public void removeSession(Long challengeId, Long userId) {
        String sessionKey = makeSessionKey(challengeId, userId);
        
        // 1. 메모리에서 세션 제거
        WebSocketSession session = sessions.remove(sessionKey);
        
        // 2. Redis에서 참가자 제거
        String participantsKey = String.format(PARTICIPANT_KEY_PREFIX, challengeId);
        redisTemplate.opsForZSet().remove(participantsKey, userId.toString());
        
        // 3. Redis에서 참가자 정보 제거
        String infoKey = String.format(PARTICIPANT_INFO_PREFIX, challengeId, userId);
        redisTemplate.delete(infoKey);
        
        log.info("세션 제거 완료: challengeId={}, userId={}, sessionId={}", 
                challengeId, userId, session != null ? session.getId() : "null");
    }

    /**
     * 세션 조회
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     * @return WebSocket 세션 (없으면 null)
     */
    public WebSocketSession getSession(Long challengeId, Long userId) {
        String sessionKey = makeSessionKey(challengeId, userId);
        return sessions.get(sessionKey);
    }

    /**
     * 챌린지의 모든 참가자 목록 조회 (본인 제외)
     * 
     * @param challengeId 챌린지 ID
     * @param excludeUserId 제외할 사용자 ID (본인)
     * @return 참가자 목록
     */
    public List<ConnectedMessage.Participant> getParticipants(Long challengeId, Long excludeUserId) {
        List<ConnectedMessage.Participant> participants = new ArrayList<>();
        
        try {
            String participantsKey = String.format(PARTICIPANT_KEY_PREFIX, challengeId);
            Set<String> userIds = redisTemplate.opsForZSet().range(participantsKey, 0, -1);
            
            if (userIds == null || userIds.isEmpty()) {
                return participants;
            }
            
            for (String userIdStr : userIds) {
                Long userId = Long.parseLong(userIdStr);
                
                // 본인 제외
                if (userId.equals(excludeUserId)) {
                    continue;
                }
                
                // 참가자 정보 조회
                String infoKey = String.format(PARTICIPANT_INFO_PREFIX, challengeId, userId);
                String infoJson = redisTemplate.opsForValue().get(infoKey);
                
                if (infoJson != null) {
                    ParticipantInfo info = objectMapper.readValue(infoJson, ParticipantInfo.class);
                    participants.add(new ConnectedMessage.Participant(
                        info.userId,
                        info.nickname,
                        info.profileImage,
                        info.distance,
                        info.pace
                    ));
                }
            }
        } catch (Exception e) {
            log.error("참가자 목록 조회 실패: challengeId={}", challengeId, e);
        }
        
        return participants;
    }

    /**
     * 챌린지의 모든 참가자 ID 목록 조회 (Redis)
     * 
     * @param challengeId 챌린지 ID
     * @return 참가자 ID 목록
     */
    public Set<String> getChallengeParticipantIds(Long challengeId) {
        try {
            String participantsKey = String.format(PARTICIPANT_KEY_PREFIX, challengeId);
            Set<String> userIds = redisTemplate.opsForZSet().range(participantsKey, 0, -1);
            return userIds != null ? userIds : Set.of();
        } catch (Exception e) {
            log.error("참가자 ID 목록 조회 실패: challengeId={}", challengeId, e);
            return Set.of();
        }
    }

    /**
     * 참가자 정보 업데이트 (distance, pace)
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     * @param distance 새로운 거리
     * @param pace 새로운 페이스
     */
    public void updateParticipantInfo(Long challengeId, Long userId, Double distance, Integer pace) {
        try {
            String infoKey = String.format(PARTICIPANT_INFO_PREFIX, challengeId, userId);
            String infoJson = redisTemplate.opsForValue().get(infoKey);
            
            if (infoJson == null) {
                log.warn("참가자 정보 없음: challengeId={}, userId={}", challengeId, userId);
                return;
            }
            
            ParticipantInfo info = objectMapper.readValue(infoJson, ParticipantInfo.class);
            ParticipantInfo updatedInfo = new ParticipantInfo(
                info.userId,
                info.nickname,
                info.profileImage,
                distance,
                pace
            );
            
            String updatedJson = objectMapper.writeValueAsString(updatedInfo);
            redisTemplate.opsForValue().set(infoKey, updatedJson);
            
            log.debug("참가자 정보 업데이트: challengeId={}, userId={}, distance={}, pace={}", 
                    challengeId, userId, distance, pace);
        } catch (Exception e) {
            log.error("참가자 정보 업데이트 실패: challengeId={}, userId={}", challengeId, userId, e);
        }
    }

    /**
     * 참가자 정보 조회
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     * @return 참가자 정보 (없으면 null)
     */
    public ParticipantInfo getParticipantInfo(Long challengeId, Long userId) {
        try {
            String infoKey = String.format(PARTICIPANT_INFO_PREFIX, challengeId, userId);
            String infoJson = redisTemplate.opsForValue().get(infoKey);
            
            if (infoJson == null) {
                return null;
            }
            
            return objectMapper.readValue(infoJson, ParticipantInfo.class);
        } catch (Exception e) {
            log.error("참가자 정보 조회 실패: challengeId={}, userId={}", challengeId, userId, e);
            return null;
        }
    }

    /**
     * 참가자 순위 계산 (distance 기준 내림차순)
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     * @return 순위 (1부터 시작, 없으면 null)
     */
    public Integer calculateRanking(Long challengeId, Long userId) {
        try {
            String participantsKey = String.format(PARTICIPANT_KEY_PREFIX, challengeId);
            Set<String> userIds = redisTemplate.opsForZSet().range(participantsKey, 0, -1);
            
            if (userIds == null || userIds.isEmpty()) {
                return null;
            }
            
            // 모든 참가자의 distance 조회
            List<ParticipantDistance> distances = new ArrayList<>();
            for (String userIdStr : userIds) {
                Long uid = Long.parseLong(userIdStr);
                ParticipantInfo info = getParticipantInfo(challengeId, uid);
                if (info != null) {
                    distances.add(new ParticipantDistance(uid, info.distance()));
                }
            }
            
            // distance 기준 내림차순 정렬
            distances.sort((a, b) -> Double.compare(b.distance(), a.distance()));
            
            // 순위 계산
            int ranking = 1;
            for (ParticipantDistance pd : distances) {
                if (pd.userId().equals(userId)) {
                    return ranking;
                }
                ranking++;
            }
            
            return null; // 해당 사용자를 찾지 못함
        } catch (Exception e) {
            log.error("순위 계산 실패: challengeId={}, userId={}", challengeId, userId, e);
            return null;
        }
    }

    /**
     * 세션 키 생성
     */
    private String makeSessionKey(Long challengeId, Long userId) {
        return challengeId + ":" + userId;
    }

    /**
     * 참가자 정보 DTO
     */
    public record ParticipantInfo(
        Long userId,
        String nickname,
        String profileImage,
        Double distance,
        Integer pace
    ) {}

    /**
     * 순위 계산용 DTO
     */
    private record ParticipantDistance(
        Long userId,
        Double distance
    ) {}
}

