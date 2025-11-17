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

    private final StringRedisTemplate stringRedisTemplate;  // Hash용
    private final RedisTemplate<String, String> redisTemplate; // Set용

    // redis에서 활성화된 방송리스트 담는 Set
    private static final String ACTIVE_SET = "broadcast:active";
    // 개별 방송 세션 Hash 의 prefix
    private static final String PREFIX = "broadcast:";

    private HashOperations<String, String, String> ops() {
        return stringRedisTemplate.opsForHash();
    }

    // ===== 활성 챌린지 관리 =====
    public void addActive(Long challengeId) {
        String key = PREFIX + challengeId;

        // createdAt 없으면 초기값 넣기
        ops().putIfAbsent(key, "createdAt", LocalDateTime.now().toString());

        // viewerCount 없으면 0 넣기
        ops().putIfAbsent(key, "viewerCount", "0");

        // ACTIVE SET 등록
        redisTemplate.opsForSet().add(ACTIVE_SET, challengeId.toString());
    }

    public void removeActive(Long challengeId) {
        String key = PREFIX + challengeId;
        redisTemplate.opsForSet().remove(ACTIVE_SET, challengeId.toString());
        redisTemplate.delete(key); // 깔끔하게 정리 가능
    }

    public Set<String> getActiveChallengeIds() {
        Set<String> ids = stringRedisTemplate.opsForSet().members(ACTIVE_SET);
        return ids != null ? ids : Collections.emptySet();
    }

    // 시청자 수 증가(+1)
    // 최대 100명 허용
    public void increaseViewer(Long challengeId) {
        String key = PREFIX + challengeId;

        Long newValue = ops().increment(key, "viewerCount", 1);

        if (newValue != null && newValue > 100) {
            ops().put(key, "viewerCount", "100");
        }
    }

    public void decreaseViewer(Long challengeId) {
        String key = PREFIX + challengeId;

        Long newValue = ops().increment(key, "viewerCount", -1);

        if (newValue != null && newValue < 0) {
            ops().put(key, "viewerCount", "0");
        }
    }

    /** ---------- 조회용 ---------- */

    public int getViewerCount(Long challengeId) {
        String key = PREFIX + challengeId;
        String value = ops().get(key, "viewerCount");
        try {
            return value != null ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getCreatedAt(Long challengeId) {
        String key = PREFIX + challengeId;
        String value = ops().get(key, "createdAt");
        return value != null ? value : LocalDateTime.now().toString();
    }


}
