package com.runnity.challenge.service;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.repository.ChallengeRepository;
import com.runnity.global.exception.GlobalException;
import com.runnity.global.status.ErrorStatus;
import com.runnity.notification.domain.NotificationOutbox;
import com.runnity.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeStateService {

    private final ChallengeRepository challengeRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;

    /**
     * 챌린지 상태를 RECRUITING → READY로 변경하고 알림 Outbox 생성
     * @param challengeId 챌린지 ID
     */
    @Transactional
    public void handleReady(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND));

        challenge.ready();

        // 알림 Outbox 생성
        NotificationOutbox outbox = NotificationOutbox.builder()
                .challengeId(challengeId)
                .type(NotificationOutbox.NotificationType.CHALLENGE_READY)
                .build();
        notificationOutboxRepository.save(outbox);

        log.info("챌린지 READY 상태 전환 완료: challengeId={}", challengeId);
    }

    /**
     * 챌린지 상태를 READY → RUNNING으로 변경
     * @param challengeId 챌린지 ID
     */
    @Transactional
    public void handleRunning(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND));

        challenge.run();

        log.info("챌린지 RUNNING 상태 전환 완료: challengeId={}", challengeId);
    }

    /**
     * 챌린지 상태를 RUNNING → DONE으로 변경하고 알림 Outbox 생성
     * @param challengeId 챌린지 ID
     */
    @Transactional
    public void handleDone(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new GlobalException(ErrorStatus.CHALLENGE_NOT_FOUND));

        challenge.complete();

        // 알림 Outbox 생성
        NotificationOutbox outbox = NotificationOutbox.builder()
                .challengeId(challengeId)
                .type(NotificationOutbox.NotificationType.CHALLENGE_DONE)
                .build();
        notificationOutboxRepository.save(outbox);

        log.info("챌린지 DONE 상태 전환 완료: challengeId={}", challengeId);
    }
}

