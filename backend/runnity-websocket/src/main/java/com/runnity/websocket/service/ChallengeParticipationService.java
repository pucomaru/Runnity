package com.runnity.websocket.service;

import com.runnity.websocket.domain.ChallengeParticipation;
import com.runnity.websocket.repository.ChallengeParticipationRepository;
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

    private final ChallengeParticipationRepository participationRepository;

    /**
     * 참가자 상태 업데이트
     * 
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID (memberId)
     * @param reason 퇴장 사유 (QUIT, FINISH, TIMEOUT, DISCONNECTED, KICKED, ERROR)
     */
    @Transactional
    public void updateParticipationStatus(Long challengeId, Long userId, String reason) {
        try {
            // 1. ChallengeParticipation 엔티티 조회
            ChallengeParticipation participation = participationRepository
                    .findByChallengeIdAndMemberId(challengeId, userId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("참가 정보를 찾을 수 없습니다: challengeId=%d, userId=%d", challengeId, userId)));

            // 2. reason에 따라 ParticipationStatus 매핑 및 상태 업데이트
            switch (reason) {
                case "QUIT" -> participation.quit();
                case "FINISH" -> participation.complete();
                case "TIMEOUT" -> participation.timeout();
                case "DISCONNECTED" -> participation.disconnect();
                case "KICKED" -> participation.kick();
                case "ERROR" -> participation.markAsError();
                case "EXPIRED" -> {
                    log.info("EXPIRED 상태는 비즈니스 서버에서 처리합니다: challengeId={}, userId={}", challengeId, userId);
                    return;
                }
                default -> throw new IllegalArgumentException("알 수 없는 reason: " + reason);
            }

            // 3. 상태 업데이트 및 저장
            participationRepository.save(participation);
            
            log.info("참가자 상태 업데이트 완료: challengeId={}, userId={}, reason={}, status={}", 
                    challengeId, userId, reason, participation.getStatus());
            
        } catch (IllegalArgumentException e) {
            log.error("참가자 상태 업데이트 실패 (잘못된 요청): challengeId={}, userId={}, reason={}, error={}", 
                    challengeId, userId, reason, e.getMessage());
            // DB 업데이트 실패해도 다른 로직은 계속 진행
        } catch (Exception e) {
            log.error("참가자 상태 업데이트 실패: challengeId={}, userId={}, reason={}", 
                    challengeId, userId, reason, e);
            // DB 업데이트 실패해도 다른 로직은 계속 진행
        }
    }
}

