package com.runnity.stream.socket.util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Redis에 저장된 방송 시청자 수(viewerCount)를 관리하는 서비스
 *
 * - 방송별 viewerCount 키는 "broadcast:{challengeId}:viewerCount" 형태
 * - 연결 시 +1, 종료 시 -1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ViewerCountService {

    private final BroadcastRedisUtil redisUtil;

    // 시청자 수 증가
    public void increment(Long challengeId){
        try {
            redisUtil.increaseViewer(challengeId);
            log.debug("viewer + 1 | challengeId = {}", challengeId);
        }catch (Exception e){
            log.error("Failed to increment viewerCount: {}", e.getMessage());
        }
    }
    
    // 시청자 수 감소
    public void decrement(Long challengeId){
        try{
            redisUtil.decreaseViewer(challengeId);
            log.debug("Viewer -1 | challengeId = {}", challengeId);
        }catch (Exception e){
            log.error("Failed to decrement viewerCount: {}", e.getMessage());
        }

    }



}
