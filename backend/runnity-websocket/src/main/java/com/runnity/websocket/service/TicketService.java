package com.runnity.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.websocket.exception.InvalidTicketException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket 티켓 검증 및 소모 서비스
 * Redis에서 티켓을 조회하고 검증한 후 즉시 삭제합니다.
 */
@Slf4j
@Service
public class TicketService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "ws_ticket:";

    public TicketService(
            @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 티켓 검증 및 소모 (일회성)
     * 
     * @param ticket 티켓 UUID
     * @return 티켓 데이터 (userId, challengeId, ticketType)
     * @throws InvalidTicketException 유효하지 않은 티켓인 경우
     */
    public TicketData verifyAndConsumeTicket(String ticket) {
        String key = KEY_PREFIX + ticket;
        
        try {
            // Redis에서 티켓 조회
            String ticketJson = redisTemplate.opsForValue().get(key);
            
            if (ticketJson == null) {
                log.warn("유효하지 않은 티켓: {}", ticket);
                throw new InvalidTicketException("유효하지 않거나 만료된 티켓입니다.");
            }
            
            // 티켓 즉시 삭제 (일회성 보장)
            redisTemplate.delete(key);
            
            // JSON 파싱
            TicketData ticketData = objectMapper.readValue(ticketJson, TicketData.class);
            
            log.info("티켓 검증 및 소모 완료: ticket={}, userId={}, challengeId={}, type={}",
                    ticket, ticketData.userId(), ticketData.challengeId(), ticketData.ticketType());
            
            return ticketData;
        } catch (InvalidTicketException e) {
            throw e;
        } catch (Exception e) {
            log.error("티켓 검증 실패: ticket={}", ticket, e);
            throw new InvalidTicketException("티켓 검증 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 티켓 데이터 DTO
     */
    public record TicketData(
        Long userId,
        Long challengeId,
        String ticketType,
        String nickname,
        String profileImage
    ) {}
}

