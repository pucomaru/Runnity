package com.runnity.stream.socket.util;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * 방송 중 참가자별 러너 상태 관리 및 랭킹 매핑 전용 Redis 유틸
 */
@Component
@RequiredArgsConstructor
public class BroadcastRunnerRedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    private String runnersKey(Long challengeId) {
        return "broadcast:" + challengeId + ":runners";
    }

    private String rankKey(Long challengeId) {
        return "broadcast:" + challengeId + ":rank";
    }

    /* ========== 러너 상태 관리 ========== */

    public void updateRunnerProgress(Long challengeId,
                                     Long runnerId,
                                     String nickname,
                                     String profileImage,
                                     Double distance,
                                     Double pace,
                                     Integer ranking) {

        String key = runnersKey(challengeId);
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        String prefix = runnerId + ":";

        if (nickname != null) ops.put(key, prefix + "nickname", nickname);
        if (profileImage != null) ops.put(key, prefix + "profileImage", profileImage);
        if (distance != null) ops.put(key, prefix + "distance", distance);
        if (pace != null) ops.put(key, prefix + "pace", pace);
        if (ranking != null) ops.put(key, prefix + "ranking", ranking);
    }

    public Map<String, Object> getRunnerData(Long challengeId, Long runnerId) {
        String key = runnersKey(challengeId);
        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();
        String prefix = runnerId + ":";

        Map<String, Object> map = new HashMap<>();
        map.put("nickname", ops.get(key, prefix + "nickname"));
        map.put("profileImage", ops.get(key, prefix + "profileImage"));
        map.put("distance", ops.get(key, prefix + "distance"));
        map.put("pace", ops.get(key, prefix + "pace"));
        map.put("ranking", ops.get(key, prefix + "ranking"));

        if (map.values().stream().allMatch(Objects::isNull)) return null;
        return map;
    }

    /* ========== rank → runnerId 매핑 관리 ========== */

    public Long getRunnerIdByRank(Long challengeId, int rank) {
        String key = rankKey(challengeId);
        Object v = redisTemplate.opsForHash().get(key, String.valueOf(rank));
        if (v == null) return null;
        return Long.parseLong(v.toString());
    }

    public void setRankOwner(Long challengeId, int rank, Long runnerId) {
        String key = rankKey(challengeId);
        redisTemplate.opsForHash().put(key, String.valueOf(rank), runnerId.toString());
    }
}
