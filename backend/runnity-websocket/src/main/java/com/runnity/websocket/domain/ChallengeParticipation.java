package com.runnity.websocket.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "challenge_participation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_challenge_member",
                        columnNames = {"challenge_id", "member_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationStatus status;

    @Column
    private Integer ranking;

    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "run_record_id")
    private Long runRecordId;

    /**
     * 자발적 포기 처리 (RUNNING → QUIT)
     */
    public void quit() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new IllegalStateException("RUNNING 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.status = ParticipationStatus.QUIT;
    }

    /**
     * 챌린지 완주 처리 (RUNNING → COMPLETED)
     */
    public void complete() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new IllegalStateException("RUNNING 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.status = ParticipationStatus.COMPLETED;
    }

    /**
     * 무응답 처리 (RUNNING → TIMEOUT)
     */
    public void timeout() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new IllegalStateException("RUNNING 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.status = ParticipationStatus.TIMEOUT;
    }

    /**
     * 연결 끊김 처리 (RUNNING → DISCONNECTED)
     */
    public void disconnect() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new IllegalStateException("RUNNING 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.status = ParticipationStatus.DISCONNECTED;
    }

    /**
     * 강제 퇴장 처리 (RUNNING → KICKED)
     */
    public void kick() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new IllegalStateException("RUNNING 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.status = ParticipationStatus.KICKED;
    }

    /**
     * 오류 처리 (RUNNING → ERROR)
     */
    public void markAsError() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new IllegalStateException("RUNNING 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.status = ParticipationStatus.ERROR;
    }

}

