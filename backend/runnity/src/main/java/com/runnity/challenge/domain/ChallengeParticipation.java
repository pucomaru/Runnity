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
        if (this.status == ParticipationStatus.LEFT) {
            throw new GlobalException(ErrorStatus.CHALLENGE_ALREADY_LEFT);
        }
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
        return ParticipationStatus.ACTIVE_PARTICIPATION_STATUSES.contains(this.status);
    }
}
