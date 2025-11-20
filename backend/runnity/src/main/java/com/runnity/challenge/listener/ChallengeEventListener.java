package com.runnity.challenge.listener;

import com.runnity.challenge.service.ChallengeStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis Pub/Sub 이벤트 리스너
 * challenge:*:ready, challenge:*:running, challenge:*:done 패턴을 구독하여
 * 챌린지 상태 전환을 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeEventListener implements MessageListener {

    private final ChallengeStateService challengeStateService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
            String body = new String(message.getBody(), StandardCharsets.UTF_8);

            log.info("Redis Pub/Sub 이벤트 수신: channel={}, body={}", channel, body);

            // 채널에서 challengeId 추출
            // 예: "challenge:123:ready" -> challengeId = 123
            Long challengeId = extractChallengeId(channel);
            if (challengeId == null) {
                log.warn("채널에서 challengeId를 추출할 수 없습니다: channel={}", channel);
                return;
            }

            // 채널 패턴에 따라 적절한 핸들러 호출
            if (channel.endsWith(":ready")) {
                challengeStateService.handleReady(challengeId);
            } else if (channel.endsWith(":running")) {
                challengeStateService.handleRunning(challengeId);
            } else if (channel.endsWith(":done")) {
                challengeStateService.handleDone(challengeId);
            } else {
                log.warn("알 수 없는 채널 패턴: channel={}", channel);
            }

        } catch (Exception e) {
            log.error("Redis Pub/Sub 이벤트 처리 중 오류 발생", e);
        }
    }

    /**
     * 채널 이름에서 challengeId 추출
     * 예: "challenge:123:ready" -> 123
     * 
     * @param channel Redis 채널 이름
     * @return challengeId, 추출 실패 시 null
     */
    private Long extractChallengeId(String channel) {
        try {
            // "challenge:123:ready" 형식에서 challengeId 추출
            String[] parts = channel.split(":");
            if (parts.length >= 2) {
                return Long.parseLong(parts[1]);
            }
        } catch (NumberFormatException e) {
            log.warn("challengeId 파싱 실패: channel={}", channel, e);
        }
        return null;
    }
}

