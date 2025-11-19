package com.runnity.broadcast.service;

import com.runnity.broadcast.client.BroadcastClient;
import com.runnity.broadcast.dto.BroadcastDto;
import com.runnity.broadcast.dto.BroadcastJoinResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
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
    private final Environment env;

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

    public BroadcastJoinResponse joinBroadcast(Long challengeId) {

        log.info("[JOIN-BROADCAST] 요청 들어옴! challengeId={}", challengeId);

        // 1) STREAM_SERVER_URL 확인
        String streamBaseUrl = System.getenv("STREAM_SERVER_URL");
        log.info("[JOIN-BROADCAST] STREAM_SERVER_URL={}", streamBaseUrl);

        if (streamBaseUrl == null) {
            log.error("[JOIN-BROADCAST] STREAM_SERVER_URL 미설정!!");
            throw new RuntimeException("STREAM_SERVER_URL not configured");
        }

        // 2) http:// → ws:// 변환
        String wsUrl = streamBaseUrl.replace("http://", "ws://") + "/ws";
        log.info("[JOIN-BROADCAST] 변환된 WS URL={}", wsUrl);

        // 3) topic 생성
        String topic = "/topic/broadcast/" + challengeId;
        log.info("[JOIN-BROADCAST] 구독 대상 topic={}", topic);

        BroadcastJoinResponse response = new BroadcastJoinResponse(wsUrl, topic, challengeId);

        log.info("[JOIN-BROADCAST] 최종 Response 생성 완료 = {}", response);

        return response;
    }
}
