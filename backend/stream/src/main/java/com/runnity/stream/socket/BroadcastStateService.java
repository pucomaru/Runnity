package com.runnity.stream.socket;

import com.runnity.stream.socket.util.BroadcastRedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BroadcastStateService {

    private final BroadcastRedisUtil redisUtil;

    public void handleReady(Long challengeId) {
        redisUtil.addActive(challengeId);
        log.info("[READY] 중계방 활성화 {}", challengeId);
    }

    public void handleRunning(Long challengeId) {
        redisUtil.addActive(challengeId);
        log.info("[RUNNING] 중계방 유지 {}", challengeId);
    }

    public void handleDone(Long challengeId) {
        redisUtil.removeActive(challengeId);
        log.info("[DONE] 중계방 비활성화 {}", challengeId);
    }
}
