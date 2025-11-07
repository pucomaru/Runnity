package com.runnity.history.repository;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeParticipation;
import com.runnity.challenge.domain.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

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



}
