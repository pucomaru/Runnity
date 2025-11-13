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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 연결용 일회성 티켓 발급 서비스
 * Redis에 TTL과 함께 티켓을 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketTicketService {

    @Qualifier("stringRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${websocket.ticket.ttl:30}")
    private int ticketTtlSeconds;

    private static final String TICKET_KEY_PREFIX = "ws_ticket:";

    /**
     * WebSocket 연결용 일회성 티켓 발급
     * 
     * @param memberId 사용자 ID
     * @param challengeId 챌린지 ID
     * @param ticketType 티켓 타입 (ENTER, REENTER)
     * @return 발급된 티켓 (UUID)
     */
    public String issueTicket(Long memberId, Long challengeId, TicketType ticketType) {
        // UUID 생성
        String ticket = UUID.randomUUID().toString();

        // 티켓 데이터 생성
        TicketData ticketData = new TicketData(memberId, challengeId, ticketType.name());

        try {
            String ticketJson = objectMapper.writeValueAsString(ticketData);

            // Redis에 저장 (setIfAbsent로 중복 방지, TTL 설정)
            String key = TICKET_KEY_PREFIX + ticket;
            Boolean success = redisTemplate.opsForValue().setIfAbsent(
                    key, 
                    ticketJson, 
                    ticketTtlSeconds, 
                    TimeUnit.SECONDS
            );

            if (Boolean.FALSE.equals(success)) {
                log.warn("티켓 중복 발생 (재시도 권장): ticket={}, memberId={}, challengeId={}",
                        ticket, memberId, challengeId);
                // 극히 드문 경우지만, 재귀 호출로 재시도
                return issueTicket(memberId, challengeId, ticketType);
            }

            log.info("WebSocket 티켓 발급: ticket={}, memberId={}, challengeId={}, type={}, ttl={}초",
                    ticket, memberId, challengeId, ticketType, ticketTtlSeconds);

            return ticket;
        } catch (Exception e) {
            log.error("티켓 발급 실패: memberId={}, challengeId={}, type={}", memberId, challengeId, ticketType, e);
            throw new GlobalException(ErrorStatus.TICKET_ISSUE_FAILED);
        }
    }

    /**
     * 티켓 타입
     */
    public enum TicketType {
        ENTER,      // 최초 입장
        REENTER     // 재입장
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
        private String ticketType;
    }

    /**
     * 티켓 TTL 반환 (클라이언트에게 제공)
     * 
     * @return TTL (초)
     */
    public int getTicketTtl() {
        return ticketTtlSeconds;
    }
}

