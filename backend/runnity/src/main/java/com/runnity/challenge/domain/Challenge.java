package com.runnity.challenge.domain;

import com.runnity.global.domain.BaseEntity;
import com.runnity.global.exception.GlobalException;
import com.runnity.global.status.ErrorStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long challengeId;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChallengeStatus status;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChallengeDistance distance;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @Column(length = 255, nullable = true)
    private String password;

    @Column(name = "is_broadcast", nullable = false)
    private Boolean isBroadcast;

    @Builder
    public Challenge(
            String title,
            Integer maxParticipants,
            LocalDateTime startAt,
            String description,
            ChallengeDistance distance,
            boolean isPrivate,
            String password,
            boolean isBroadcast
    ) {
        this.title = title;
        this.status = ChallengeStatus.RECRUITING;
        this.maxParticipants = maxParticipants;
        this.description = description;
        this.distance = distance;
        this.startAt = startAt;
        this.endAt = startAt.plusMinutes(distance.durationMinutes());
        this.isPrivate = isPrivate;
        this.password = password;
        this.isBroadcast = isBroadcast;
    }

    /**
     * 챌린지 상태를 RECRUITING → READY로 전이
     * @throws GlobalException 현재 상태가 RECRUITING이 아닌 경우
     */
    public void ready() {
        if (this.status != ChallengeStatus.RECRUITING) {
            throw new GlobalException(ErrorStatus.INVALID_STATE_TRANSITION);
        }
        this.status = ChallengeStatus.READY;
    }

    /**
     * 챌린지 상태를 READY → RUNNING으로 전이
     * @throws GlobalException 현재 상태가 READY가 아닌 경우
     */
    public void run() {
        if (this.status != ChallengeStatus.READY) {
            throw new GlobalException(ErrorStatus.INVALID_STATE_TRANSITION);
        }
        this.status = ChallengeStatus.RUNNING;
    }

    /**
     * 챌린지 상태를 RUNNING → DONE으로 전이
     * @throws GlobalException 현재 상태가 RUNNING이 아닌 경우
     */
    public void complete() {
        if (this.status != ChallengeStatus.RUNNING) {
            throw new GlobalException(ErrorStatus.INVALID_STATE_TRANSITION);
        }
        this.status = ChallengeStatus.DONE;
    }
}
