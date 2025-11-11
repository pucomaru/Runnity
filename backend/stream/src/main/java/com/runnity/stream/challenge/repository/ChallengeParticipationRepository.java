package com.runnity.stream.challenge.repository;

import com.runnity.stream.challenge.domain.ChallengeParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {

    @Query("""
        SELECT f.token
        FROM ChallengeParticipation p
        JOIN MemberFcmToken f ON p.memberId = f.memberId
        WHERE p.challengeId = :challengeId
          AND p.status IN :statuses
          AND p.isDeleted = false
          AND f.isDeleted = false
    """)
    List<String> findTokensByChallengeIdAndStatuses(
            @Param("challengeId") Long challengeId,
            @Param("statuses") List<com.runnity.stream.challenge.domain.ParticipationStatus> statuses
    );
}
