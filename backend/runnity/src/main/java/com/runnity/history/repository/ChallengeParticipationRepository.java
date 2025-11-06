package com.runnity.history.repository;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeParticipation;
import com.runnity.challenge.domain.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {

    @Query("SELECT cp.challenge FROM ChallengeParticipation cp " +
            "WHERE cp.member.memberId = :memberId " +
            "AND cp.status = :status " +
            "ORDER BY cp.challenge.startAt ASC")
    List<Challenge> findChallengesByMemberIdAndStatus(
            @Param("memberId") Long memberId,
            @Param("status") ParticipationStatus status
    );


    @Query("SELECT cp.challenge.challengeId, COUNT(cp) " +
            "FROM ChallengeParticipation cp " +
            "WHERE cp.challenge.challengeId IN :challengeIds " +
            "GROUP BY cp.challenge.challengeId")
    List<Object[]> countByChallengeIds(@Param("challengeIds") List<Long> challengeIds);



}
