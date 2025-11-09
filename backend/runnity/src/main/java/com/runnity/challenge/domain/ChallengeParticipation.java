package com.runnity.challenge.domain;

import com.runnity.global.domain.BaseEntity;
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
     * 챌린지 참가 처리
     * 새로운 참가 신청 시 사용
     */
    public static ChallengeParticipation join(Challenge challenge, Member member) {
        return ChallengeParticipation.builder()
                .challenge(challenge)
                .member(member)
                .build();
    }

    /**
     * 참가 취소 처리
     * 
     * @throws IllegalStateException 이미 취소한 상태이거나 취소할 수 없는 상태인 경우
     */
    public void cancel() {
        if (this.status == ParticipationStatus.LEFT) {
            throw new IllegalStateException("이미 참가 취소한 상태입니다.");
        }
        if (!canCancel()) {
            throw new IllegalStateException("현재 상태에서는 참가 취소할 수 없습니다. 현재 상태: " + this.status);
        }
        this.status = ParticipationStatus.LEFT;
    }

    /**
     * 재참가 처리 (LEFT 상태에서 WAITING으로 변경)
     * 
     * @throws IllegalStateException LEFT 상태가 아닌 경우
     */
    public void rejoin() {
        if (this.status != ParticipationStatus.LEFT) {
            throw new IllegalStateException("재참가할 수 없는 상태입니다. 현재 상태: " + this.status);
        }
        this.status = ParticipationStatus.WAITING;
    }

    /**
     * 참가 취소 가능 여부 확인
     */
    private boolean canCancel() {
        // RUNNING, COMPLETED 등은 취소 불가능할 수 있음
        // 현재는 모든 상태에서 취소 가능하도록 설정
        return this.status != ParticipationStatus.LEFT;
    }

    /**
     * 활성 참가 상태인지 확인 (LEFT 제외)
     */
    public boolean isActive() {
        return ParticipationStatus.ACTIVE_PARTICIPATION_STATUSES.contains(this.status);
    }
}
