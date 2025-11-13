package com.runnity.stream.socket.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

//Redis의 broadcast:* 키 관리 전담

@Component
@RequiredArgsConstructor
public class BroadcastRedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;

    // redis에서 활성화된 방송리스트 담는 Set
    private static final String ACTIVE_SET = "broadcast:active";

    // 개별 방송 세션 Hash 의 prefix
    private static final String PREFIX = "broadcast:";

    // 방송 세션(Hash: broadcast:{challengeId}) 존재 여부 확인
    public boolean exists(Long challengeId){
        String key = PREFIX + challengeId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /*
    방송 세션 생성
    challengeId 를 기반으로 Redis Hash에 방송 정보 저장,
    broadcast:active Set에 challengeId추가
     */
    public void createSession(Long challengeId, String title, int participantCount){

        String key = PREFIX + challengeId;
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();

        hashOps.put(key, "title", title);
        hashOps.put(key, "status", "WAITING");
        hashOps.put(key, "viewerCount", 0);
        hashOps.put(key, "participantCount", participantCount);
        hashOps.put(key, "createdAt", LocalDateTime.now().toString());

        // 활성 방송 목록에 추가
        redisTemplate.opsForSet().add(ACTIVE_SET, challengeId.toString());
    }

    // 방송 상태 갱신
    // WAITING -> LIVE -> ENDED
    public void updateStatus(Long challengeId, String status){
        String key = PREFIX + challengeId;
        redisTemplate.opsForHash().put(key,"status",status);
    }

    // 시청자 수 증가(+1)
    // 최대 100명 허용
    public void increaseViewer(Long challengeId){
        String key = PREFIX + challengeId;
        Long viewerCount = redisTemplate.opsForHash().increment(key, "viewerCount",1);

        if (viewerCount != null && viewerCount > 100){
            redisTemplate.opsForHash().put(key,"viewerCount",100);
        }
    }

    // 시청자 수 감소(-1)
    public void decreaseViewer(Long challengeId){
        String key = PREFIX + challengeId;
        Long viewerCount = redisTemplate.opsForHash().increment(key, "viewerCount",-1);

        if (viewerCount != null && viewerCount < 0){
            redisTemplate.opsForHash().put(key,"viewerCount",0);
        }
    }

    // 세션 만료 처리
    // 방송 종료 시 broadcast:{challengeId} Hash 삭제 및 active Set에서 재거
    public void expireSession(Long challengeId){
        String key = PREFIX + challengeId;

        // 상태를 ENDED로 바꾸고 1시간 뒤 자동 삭제
        redisTemplate.opsForHash().put(key, "status", "ENDED");
        redisTemplate.expire(key, Duration.ofHours(1));
        redisTemplate.opsForSet().remove(ACTIVE_SET, challengeId.toString());

    }

    // 현지 활성화된 방송 세션 목록 조회
    // broadcast:active Set에 등록된 모든 challengeId 기반으로 Hash 조회
    public List<Map<String,String>> getActiveSessions(){
        Set<String> ids = stringRedisTemplate.opsForSet().members(ACTIVE_SET);
        if (ids == null) return Collections.emptyList();

        List<Map<String,String>> sessions = new ArrayList<>();
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();

        for (String id:ids){
            sessions.add(hashOps.entries(PREFIX +id));
        }
        return sessions;

    }

    // 방송 메타(Hash: broadcast:{challengeId}:meta) 존재 여부 확인
    public boolean existsMeta(Long challengeId) {
        String key = "broadcast:" + challengeId + ":meta";
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void createMetaSession(Long challengeId, String title, int participantCount, double distance, boolean isBroadcast) {
        String key = "broadcast:" + challengeId + ":meta";
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();

        hashOps.put(key, "title", title);
        hashOps.put(key, "participantCount", participantCount);
        hashOps.put(key, "distance", distance);
        hashOps.put(key, "isBroadcast", isBroadcast);
        hashOps.put(key, "createdAt", LocalDateTime.now().toString());
    }

    // 방송 총 거리(distance) 조회용
    public double getTotalDistance(Long challengeId) {
        String key = "broadcast:" + challengeId + ":meta";
        Object value = redisTemplate.opsForHash().get(key, "distance");
        if (value == null) return 0.0;
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
