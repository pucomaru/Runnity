package com.runnity.stream.socket.util;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
// 비즈니스 meta 읽는 유틸 추가

@Component
@RequiredArgsConstructor
public class ChallengeMetaRedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private String metaKey(Long challengeId) {
        // 챌린지 팀 설계: challenge:{id}:meta
        return "challenge:" + challengeId + ":meta";
    }

    public Map<String, String> getMeta(Long challengeId) {
        String key = metaKey(challengeId);
        HashOperations<String, String, String> ops = stringRedisTemplate.opsForHash();
        Map<String, String> meta = ops.entries(key);
        if (meta == null || meta.isEmpty()) {
            return Collections.emptyMap();
        }
        return meta;
    }

}
