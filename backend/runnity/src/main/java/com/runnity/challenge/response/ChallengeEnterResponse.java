package com.runnity.challenge.response;

/**
 * 챌린지 입장 응답 DTO
 * 
 * @param ticket WebSocket 연결용 일회성 티켓 (UUID)
 * @param userId 사용자 ID
 * @param challengeId 챌린지 ID
 * @param wsUrl WebSocket 서버 URL
 */
public record ChallengeEnterResponse(
        String ticket,
        Long userId,
        Long challengeId,
        String wsUrl
) {
}

