package com.runnity.websocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 챌린지 참가자 상태 업데이트 서비스
 * 
 * 웹소켓 서버에서 참가자 상태를 DB에 직접 업데이트합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeParticipationService {

    /**
     * 참가자 상태 업데이트
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     * @param reason 퇴장 사유 (QUIT, FINISH, TIMEOUT, DISCONNECTED, KICKED, ERROR, EXPIRED)
     */
    @Transactional
    public void updateParticipationStatus(Long challengeId, Long userId, String reason) {
        try {
            // TODO: 엔티티와 리포지토리 추가 후 구현
            // 1. ChallengeParticipation 엔티티 조회
            // 2. reason에 따라 ParticipationStatus 매핑
            //    - QUIT → QUIT
            //    - FINISH → COMPLETE
            //    - TIMEOUT → TIMEOUT
            //    - DISCONNECTED → DISCONNECTED
            //    - KICKED → KICKED
            //    - ERROR → ERROR
            //    - EXPIRED → EXPIRED
            // 3. 상태 업데이트 및 저장
            
            log.info("참가자 상태 업데이트: challengeId={}, userId={}, reason={}", 
                    challengeId, userId, reason);
            
        } catch (Exception e) {
            log.error("참가자 상태 업데이트 실패: challengeId={}, userId={}, reason={}", 
                    challengeId, userId, reason, e);
            // DB 업데이트 실패해도 다른 로직은 계속 진행
        }
    }
}

