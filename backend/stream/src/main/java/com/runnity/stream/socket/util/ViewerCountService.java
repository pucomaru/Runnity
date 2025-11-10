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



}
