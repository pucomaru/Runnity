package com.runnity.stream.socket.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BroadcastRunnerRedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private String runnersKey(Long challengeId) {
        return "broadcast:" + challengeId + ":runners";
    }

    private String rankKey(Long challengeId) {
        return "broadcast:" + challengeId + ":rank";
    }

    private HashOperations<String, String, String> ops() {
        return stringRedisTemplate.opsForHash();
    }

    /* ========== 러너 상태 저장 ========== */
    public void updateRunnerProgress(
            Long challengeId,
            Long runnerId,
            String nickname,
            String profileImage,
            Double distance,
            Double pace,
            Integer ranking
    ) {
        String key = runnersKey(challengeId);
        String prefix = runnerId + ":";

        if (nickname != null) ops().put(key, prefix + "nickname", nickname);
        if (profileImage != null) ops().put(key, prefix + "profileImage", profileImage);
        if (distance != null) ops().put(key, prefix + "distance", distance.toString());
        if (pace != null) ops().put(key, prefix + "pace", pace.toString());
        if (ranking != null) ops().put(key, prefix + "ranking", ranking.toString());
    }

    /* ========== 조회 ========== */
    public Map<String, Object> getRunnerData(Long challengeId, Long runnerId) {
        String key = runnersKey(challengeId);
        String prefix = runnerId + ":";

        Map<String, Object> map = new HashMap<>();
        map.put("nickname", ops().get(key, prefix + "nickname"));
        map.put("profileImage", ops().get(key, prefix + "profileImage"));
        map.put("distance", ops().get(key, prefix + "distance"));
        map.put("pace", ops().get(key, prefix + "pace"));
        map.put("ranking", ops().get(key, prefix + "ranking"));

        if (map.values().stream().allMatch(Objects::isNull)) return null;
        return map;
    }

    /* ========== 순위 → runnerId 매핑 ========== */
    public Long getRunnerIdByRank(Long challengeId, int rank) {
        String key = rankKey(challengeId);
        String v = ops().get(key, String.valueOf(rank));
        if (v == null) return null;
        return Long.parseLong(v);
    }

    public void setRankOwner(Long challengeId, int rank, Long runnerId) {
        String key = rankKey(challengeId);
        ops().put(key, String.valueOf(rank), runnerId.toString());
    }
}
