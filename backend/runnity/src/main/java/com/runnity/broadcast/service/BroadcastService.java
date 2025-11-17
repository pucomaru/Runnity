package com.runnity.broadcast.service;

import com.runnity.broadcast.client.BroadcastClient;
import com.runnity.broadcast.dto.BroadcastDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 중계 서비스
 * - 프론트에서 요청을 받아 Stream 서버로 위임
 * - Stream 서버에서 Redis 상태 기반으로 방송 목록 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastService {

    private final BroadcastClient broadcastClient;

    @Retryable(value = {RuntimeException.class}, maxAttempts = 2, backoff = @Backoff(delay = 1000))
    public List<BroadcastDto> getActiveBroadcasts(
            String keyword,
            List<String> distance,
            String sort
    ) {
        try {
            List<BroadcastDto> broadcasts = broadcastClient.getActiveBroadcasts(keyword, distance, sort);
            log.info("Successfully fetched {} active broadcasts", broadcasts.size());
            return broadcasts;
        } catch (Exception e) {
            log.error("Failed to fetch active broadcasts: {}", e.getMessage());
            // 에러 발생 시 빈 리스트 반환 또는 예외 처리
            return Collections.emptyList();
        }
    }
}
