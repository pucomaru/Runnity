package com.runnity.challenge.service;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeParticipation;
import com.runnity.challenge.domain.ChallengeStatus;
import com.runnity.challenge.domain.ParticipationStatus;
import com.runnity.challenge.repository.ChallengeParticipationRepository;
import com.runnity.challenge.repository.ChallengeRepository;
import com.runnity.global.exception.GlobalException;
import com.runnity.global.status.ErrorStatus;
import com.runnity.notification.domain.NotificationOutbox;
import com.runnity.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeStateService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository participationRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    @Qualifier("stringRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 챌린지 상태를 RECRUITING → READY로 변경하고 알림 Outbox 생성
     * 챌린지 시작 5분 전에 호출되며, Redis에 meta 정보를 저장합니다.
     * 
     * 중복 실행 방지: 이미 READY 이상 상태이면 무시
     * 
     * @param challengeId 챌린지 ID
     */
    @Transactional
    public void handleReady(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND));

        // 중복 실행 방지: 이미 READY 이상 상태이면 무시
        if (challenge.getStatus() != ChallengeStatus.RECRUITING) {
            log.debug("이미 처리된 챌린지: challengeId={}, status={}", challengeId, challenge.getStatus());
            return;
        }

        challenge.ready();

        // Redis에 챌린지 meta 정보 저장 (웹소켓 서버에서 조회용)
        saveChallengeMetaToRedis(challengeId, challenge);

        // 알림 Outbox 생성
        NotificationOutbox outbox = NotificationOutbox.builder()
                .challengeId(challengeId)
                .type(NotificationOutbox.NotificationType.CHALLENGE_READY)
                .build();
        notificationOutboxRepository.save(outbox);

        log.info("챌린지 READY 상태 전환 완료: challengeId={}, distance={}, isBroadcast={}", 
                challengeId, challenge.getDistance().value(), challenge.getIsBroadcast());
    }

    /**
     * Redis에 챌린지 meta 정보 저장
     * challenge:{challengeId}:meta에 다음 정보 저장:
     * - title: 챌린지 제목
     * - totalApplicantCount: 전체 신청자 수 (TOTAL_APPLICANT_STATUSES)
     * - actualParticipantCount: 실제 참여자 수 (초기값 0, 입장 시 +1)
     * - distance: 목표 거리 (km)
     * - isBroadcast: 브로드캐스트 여부
     * 
     * @param challengeId 챌린지 ID
     * @param challenge 챌린지 엔티티
     */
    private void saveChallengeMetaToRedis(Long challengeId, Challenge challenge) {
        try {
            String metaKey = "challenge:" + challengeId + ":meta";
            
            // 1. title 저장
            redisTemplate.opsForHash().put(metaKey, "title", challenge.getTitle());
            
            // 2. totalApplicantCount 조회 및 저장 (TOTAL_APPLICANT_STATUSES)
            long totalApplicantCount = participationRepository.findByChallengeIdAndActiveStatus(
                    challengeId, ParticipationStatus.TOTAL_APPLICANT_STATUSES).size();
            redisTemplate.opsForHash().put(metaKey, "totalApplicantCount", String.valueOf(totalApplicantCount));
            
            // 3. actualParticipantCount 초기값 0 저장 (입장 시 +1)
            redisTemplate.opsForHash().put(metaKey, "actualParticipantCount", "0");
            
            // 4. distance 저장
            String distanceValue = String.valueOf(challenge.getDistance().value());
            redisTemplate.opsForHash().put(metaKey, "distance", distanceValue);
            
            // 5. isBroadcast 저장
            String isBroadcastValue = Boolean.TRUE.equals(challenge.getIsBroadcast()) ? "true" : "false";
            redisTemplate.opsForHash().put(metaKey, "isBroadcast", isBroadcastValue);

            log.info("챌린지 meta 저장 완료: challengeId={}, title={}, totalApplicantCount={}, distance={}, isBroadcast={}", 
                    challengeId, challenge.getTitle(), totalApplicantCount, distanceValue, isBroadcastValue);
        } catch (Exception e) {
            log.error("챌린지 meta 저장 실패: challengeId={}", challengeId, e);
            // Redis 저장 실패해도 챌린지 상태 전환은 계속 진행
        }
    }

    /**
     * 챌린지 상태를 READY → RUNNING으로 변경
     * 참여하지 않은 WAITING 상태 사용자를 NOT_STARTED로 변경
     * 
     * 중복 실행 방지: 이미 RUNNING 이상 상태이면 무시
     * 
     * @param challengeId 챌린지 ID
     */
    @Transactional
    public void handleRunning(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND));

        // 중복 실행 방지: 이미 RUNNING 이상 상태이면 무시
        if (challenge.getStatus() != ChallengeStatus.READY) {
            log.debug("이미 처리된 챌린지: challengeId={}, status={}", challengeId, challenge.getStatus());
            return;
        }

        challenge.run();

        // 참여하지 않은 WAITING 상태 참가자들을 NOT_STARTED로 변경
        List<ChallengeParticipation> waitingParticipants = participationRepository.findByChallengeIdAndStatus(
                challengeId, ParticipationStatus.WAITING);

        for (ChallengeParticipation participation : waitingParticipants) {
            try {
                participation.markAsNotStarted();
                participationRepository.save(participation);
                log.debug("참가자 상태 NOT_STARTED로 변경: challengeId={}, userId={}", 
                        challengeId, participation.getMember().getMemberId());
            } catch (Exception e) {
                log.error("참가자 NOT_STARTED 처리 실패: challengeId={}, participantId={}", 
                        challengeId, participation.getParticipantId(), e);
            }
        }

        log.info("챌린지 RUNNING 상태 전환 완료: challengeId={}, notStartedCount={}", 
                challengeId, waitingParticipants.size());
    }

    /**
     * 챌린지 상태를 RUNNING → DONE으로 변경하고 알림 Outbox 생성
     * 
     * 중복 실행 방지: 이미 DONE 상태이면 무시
     * EXPIRED 처리: RUNNING 상태 참가자를 EXPIRED로 변경
     * 
     * @param challengeId 챌린지 ID
     */
    @Transactional
    public void handleDone(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND));

        // 중복 실행 방지: 이미 DONE 상태이면 무시
        if (challenge.getStatus() == ChallengeStatus.DONE) {
            log.debug("이미 DONE 상태인 챌린지: challengeId={}", challengeId);
            return;
        }

        // RUNNING 상태가 아니면 에러
        if (challenge.getStatus() != ChallengeStatus.RUNNING) {
            log.warn("RUNNING 상태가 아닌 챌린지에 대해 handleDone 호출: challengeId={}, status={}", 
                    challengeId, challenge.getStatus());
            return;
        }

        // EXPIRED 처리 대상 상태의 참가자들을 EXPIRED로 변경
        List<ChallengeParticipation> expiredTargetParticipants = participationRepository.findByChallengeIdAndStatusIn(
                challengeId, ParticipationStatus.EXPIRED_TARGET_STATUSES);

        for (ChallengeParticipation participation : expiredTargetParticipants) {
            try {
                ParticipationStatus previousStatus = participation.getStatus();
                participation.expire();
                participationRepository.save(participation);
                log.debug("참가자 상태 EXPIRED로 변경: challengeId={}, userId={}, previousStatus={}", 
                        challengeId, participation.getMember().getMemberId(), previousStatus);
            } catch (Exception e) {
                log.error("참가자 EXPIRED 처리 실패: challengeId={}, participantId={}", 
                        challengeId, participation.getParticipantId(), e);
            }
        }

        // 챌린지 상태를 DONE으로 변경
        challenge.complete();

        // 알림 Outbox 생성
        NotificationOutbox outbox = NotificationOutbox.builder()
                .challengeId(challengeId)
                .type(NotificationOutbox.NotificationType.CHALLENGE_DONE)
                .build();
        notificationOutboxRepository.save(outbox);

        log.info("챌린지 DONE 상태 전환 완료: challengeId={}, expiredCount={}", 
                challengeId, expiredTargetParticipants.size());
    }
}

