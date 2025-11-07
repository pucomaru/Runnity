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
}
