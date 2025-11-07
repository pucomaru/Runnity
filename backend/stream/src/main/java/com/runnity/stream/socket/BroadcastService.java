package com.runnity.stream.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.stream.socket.dto.RunningDataDto;
import com.runnity.stream.socket.util.BroadcastRedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/*
중계방송 관련 비즈니스 로직을 담당하는 서비스 계층
redis 접근은 BroadcastRedisUtil을 통해 수해
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastService {

    private final BroadcastRedisUtil redisUtil;
    private final BroadcastHandler broadcastHandler;    // WebSocket 브로드캐스트 담당
    private final ObjectMapper objectMapper = new ObjectMapper();

//
//    // Kafka 메시지를 수신하면 WebSocket으로 뿌릴 예정 (지금은 로그만 찍기)
//    @KafkaListener(topics = "running-data", groupId = "runnity-stream-group")
//    public void consume(String message) {
//        log.info("📩 Kafka 메시지 수신: {}", message);
//        // 나중에 broadcastHandler.sendToAll(message); 로 연결 예정
//    }

    // kafka 이벤트 수신
    @KafkaListener(topics ="running-data", groupId = "runnity-stream-group")
    public void consume(String message) throws JsonProcessingException {
        // 1. kafka 메시지 json -> dto변환
        RunningDataDto data = objectMapper.readValue(message, RunningDataDto.class);
        Long challengeId = data.getChallengeId();
        
        // 2. 방송 대상인 경우 세션 존재 확인 후 자동 생성
        if(data.isBroadcast()){
            if (!redisUtil.exists(challengeId)){
                log.info("방송 세션 자동 생성: challengeId={}, title={}", challengeId, data.getTitle());
                redisUtil.createSession(challengeId, data.getTitle(), data.getParticipantCount());
                redisUtil.updateStatus(challengeId, "LIVE");
            }
        }

        //3.websocket 브로드 캐스트(추후)
        log.info("Kafka 메시지 수신 (challengeId={}): {}", challengeId, message);
        // TODO: broadcastHandler.sendToAll(challengeId, message);
        
    }

    // 챌린지 시작 시 방송 세션 생성
    public void createBroadcastSession(Long challengeId, String title, int participantCount) {
        redisUtil.createSession(challengeId, title, participantCount);
    }

    // 방송 상태 변경(WAITING, LIVE, ENDED)
    public void updateStatus(Long challengeId, String status) {
        redisUtil.updateStatus(challengeId, status);
    }

    // 시청자 입장시 +1
    public void addViewer(Long challengeId) {
        redisUtil.increaseViewer(challengeId);
    }

    // 시청자 퇴장시 -1
    public void removeViewer(Long challengeId) {
        redisUtil.decreaseViewer(challengeId);
    }

    // 방송 종료 시 세션 제거
    public void endBroadcast(Long challengeId) {
        redisUtil.expireSession(challengeId);
    }

    // 현재 활성 방송 목록 조회
    public List<Map<Object, Object>> getActiveBroadcasts() {
        return redisUtil.getActiveSessions();
    }


    
}
