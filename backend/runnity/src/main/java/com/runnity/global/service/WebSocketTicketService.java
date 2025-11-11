package com.runnity.global.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.global.exception.GlobalException;
import com.runnity.global.status.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 연결용 일회성 티켓 발급 서비스
 * Redis에 TTL 30초로 티켓을 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketTicketService {

    @Qualifier("stringRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final int TICKET_TTL_SECONDS = 30; // 30초
    private static final String TICKET_KEY_PREFIX = "ws_ticket:";

    /**
     * WebSocket 연결용 일회성 티켓 발급
     * 
     * @param memberId 사용자 ID
     * @param challengeId 챌린지 ID
     * @return 발급된 티켓 (UUID)
     */
    public String issueTicket(Long memberId, Long challengeId) {
        // UUID 생성
        String ticket = UUID.randomUUID().toString();

        // 티켓 데이터 생성
        TicketData ticketData = new TicketData(memberId, challengeId);

        try {
            String ticketJson = objectMapper.writeValueAsString(ticketData);

            // Redis에 저장 (TTL 30초)
            String key = TICKET_KEY_PREFIX + ticket;
            redisTemplate.opsForValue().set(key, ticketJson, TICKET_TTL_SECONDS, TimeUnit.SECONDS);

            log.info("WebSocket 티켓 발급: ticket={}, memberId={}, challengeId={}",
                    ticket, memberId, challengeId);

            return ticket;
        } catch (Exception e) {
            log.error("티켓 발급 실패: memberId={}, challengeId={}", memberId, challengeId, e);
            throw new GlobalException(ErrorStatus.TICKET_ISSUE_FAILED);
        }
    }

    /**
     * 티켓 데이터 DTO
     */
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TicketData {
        private Long userId;
        private Long challengeId;
    }
}

