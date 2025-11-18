package com.runnity.history.repository;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeParticipation;
import com.runnity.challenge.domain.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HistoryChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {

    @Query("SELECT cp.challenge FROM ChallengeParticipation cp " +
            "WHERE cp.member.memberId = :memberId " +
            "AND cp.status IN :statuses " +
            "ORDER BY cp.challenge.startAt ASC")
    List<Challenge> findChallengesByMemberIdAndStatus(
            @Param("memberId") Long memberId,
            @Param("statuses") Collection<ParticipationStatus> statuses
    );


    @Query("SELECT cp.challenge.challengeId, COUNT(cp) " +
            "FROM ChallengeParticipation cp " +
            "WHERE cp.challenge.challengeId IN :challengeIds " +
            "GROUP BY cp.challenge.challengeId")
    List<Object[]> countByChallengeIds(@Param("challengeIds") List<Long> challengeIds);

    /**
     * 챌린지 ID + memberId로 특정 멤버의 챌린지 참여 조회
     */
    @Query("""
        SELECT cp
        FROM ChallengeParticipation cp
        WHERE cp.member.memberId = :memberId
          AND cp.challenge.challengeId = :challengeId
          AND cp.isDeleted = false
          AND cp.status = com.runnity.challenge.domain.ParticipationStatus.COMPLETED
    """)
    Optional<ChallengeParticipation> findCompletedByMemberIdAndChallengeId(
            @Param("memberId") Long memberId,
            @Param("challengeId") Long challengeId
    );

    /**
     * 특정 챌린지의 완주자 전체 조회
     */
    @Query("""
        SELECT cp
        FROM ChallengeParticipation cp
        JOIN FETCH cp.runRecord rr
        WHERE cp.challenge.challengeId = :challengeId
          AND cp.isDeleted = false
          AND cp.status = com.runnity.challenge.domain.ParticipationStatus.COMPLETED
          AND cp.runRecord IS NOT NULL
          AND rr.isDeleted = false
        ORDER BY rr.durationSec ASC
    """)
    List<ChallengeParticipation> findFinishersForRanking(
            @Param("challengeId") Long challengeId
    );

    /**
     * runRecordId로 ChallengeParticipation 조회
     */
    @Query("""
        SELECT cp
        FROM ChallengeParticipation cp
        WHERE cp.runRecord.runRecordId = :runRecordId
          AND cp.isDeleted = false
    """)
    Optional<ChallengeParticipation> findByRunRecordId(
            @Param("runRecordId") Long runRecordId
    );
}
