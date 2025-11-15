package com.runnity.challenge.domain;

import com.runnity.global.domain.BaseEntity;
import com.runnity.global.exception.GlobalException;
import com.runnity.global.status.ErrorStatus;
import com.runnity.history.domain.RunRecord;
import com.runnity.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

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
public class ChallengeParticipation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationStatus status;

    @Column
    private Integer ranking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false, foreignKey = @ForeignKey(name = "fk_participation_challenge"))
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_participation_member"))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_record_id", foreignKey = @ForeignKey(name = "fk_participation_run_record"))
    private RunRecord runRecord;

    @Builder
    public ChallengeParticipation(
         Challenge challenge,
         Member member
    ) {
        this.status = ParticipationStatus.WAITING;
        this.challenge = challenge;
        this.member = member;
    }

    /**
     * 참가 취소 처리
     * WAITING 상태에서만 취소 가능
     * 
     * @throws GlobalException WAITING 상태가 아닌 경우
     */
    public void cancel() {
        if (this.status != ParticipationStatus.WAITING) {
            throw new GlobalException(ErrorStatus.CHALLENGE_CANCEL_NOT_ALLOWED);
        }
        this.status = ParticipationStatus.LEFT;
    }

    /**
     * 재참가 처리 (LEFT 상태에서 WAITING으로 변경)
     * 
     * @throws GlobalException LEFT 상태가 아닌 경우
     */
    public void rejoin() {
        if (this.status != ParticipationStatus.LEFT) {
            throw new GlobalException(ErrorStatus.CHALLENGE_REJOIN_NOT_ALLOWED);
        }
        this.status = ParticipationStatus.WAITING;
    }

    /**
     * 활성 참가 상태인지 확인 (LEFT 제외)
     */
    public boolean isActive() {
        return ParticipationStatus.TOTAL_APPLICANT_STATUSES.contains(this.status);
    }

    /**
     * 챌린지 입장 처리 (WAITING → RUNNING)
     * 
     * @throws GlobalException WAITING 상태가 아닌 경우
     */
    public void startRunning() {
        if (this.status != ParticipationStatus.WAITING) {
            throw new GlobalException(ErrorStatus.INVALID_PARTICIPATION_STATUS);
        }
        this.status = ParticipationStatus.RUNNING;
    }

    /**
     * 챌린지 완주 처리 (RUNNING → COMPLETED)
     * 
     * @throws GlobalException RUNNING 상태가 아닌 경우
     */
    public void complete() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new GlobalException(ErrorStatus.INVALID_PARTICIPATION_STATUS);
        }
        this.status = ParticipationStatus.COMPLETED;
    }

    /**
     * 자발적 포기 처리 (RUNNING → QUIT)
     * 
     * @throws GlobalException RUNNING 상태가 아닌 경우
     */
    public void quit() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new GlobalException(ErrorStatus.INVALID_PARTICIPATION_STATUS);
        }
        this.status = ParticipationStatus.QUIT;
    }

    /**
     * 무응답 처리 (RUNNING → TIMEOUT)
     * 
     * @throws GlobalException RUNNING 상태가 아닌 경우
     */
    public void timeout() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new GlobalException(ErrorStatus.INVALID_PARTICIPATION_STATUS);
        }
        this.status = ParticipationStatus.TIMEOUT;
    }

    /**
     * 연결 끊김 처리 (RUNNING → DISCONNECTED)
     * 
     * @throws GlobalException RUNNING 상태가 아닌 경우
     */
    public void disconnect() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new GlobalException(ErrorStatus.INVALID_PARTICIPATION_STATUS);
        }
        this.status = ParticipationStatus.DISCONNECTED;
    }

    /**
     * 강제 퇴장 처리 (RUNNING → KICKED)
     * 
     * @throws GlobalException RUNNING 상태가 아닌 경우
     */
    public void kick() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new GlobalException(ErrorStatus.INVALID_PARTICIPATION_STATUS);
        }
        this.status = ParticipationStatus.KICKED;
    }

    /**
     * 오류 처리 (RUNNING → ERROR)
     * 
     * @throws GlobalException RUNNING 상태가 아닌 경우
     */
    public void markAsError() {
        if (this.status != ParticipationStatus.RUNNING) {
            throw new GlobalException(ErrorStatus.INVALID_PARTICIPATION_STATUS);
        }
        this.status = ParticipationStatus.ERROR;
    }

    /**
     * 시간 종료 처리 (RUNNING, TIMEOUT, DISCONNECTED, ERROR, KICKED → EXPIRED)
     * 
     * @throws GlobalException 허용된 상태가 아닌 경우
     */
    public void expire() {
        if (this.status != ParticipationStatus.RUNNING
                && this.status != ParticipationStatus.TIMEOUT
                && this.status != ParticipationStatus.DISCONNECTED
                && this.status != ParticipationStatus.ERROR
                && this.status != ParticipationStatus.KICKED) {
            throw new GlobalException(ErrorStatus.INVALID_PARTICIPATION_STATUS);
        }
        this.status = ParticipationStatus.EXPIRED;
    }

    /**
     * 참여 안함 처리 (WAITING → NOT_STARTED)
     * 
     * @throws GlobalException WAITING 상태가 아닌 경우
     */
    public void markAsNotStarted() {
        if (this.status != ParticipationStatus.WAITING) {
            throw new GlobalException(ErrorStatus.INVALID_PARTICIPATION_STATUS);
        }
        this.status = ParticipationStatus.NOT_STARTED;
    }

    /**
     * 완주 후 RunRecord 연결
     */
    public void updateRunRecord(RunRecord runRecord) {
        this.runRecord = runRecord;
    }

    /**
     * 랭킹 업데이트
     */
    public void updateRanking(Integer ranking) {
        this.ranking = ranking;
    }
}
