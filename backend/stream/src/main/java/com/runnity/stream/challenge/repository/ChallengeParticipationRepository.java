package com.runnity.stream.challenge.repository;

import com.runnity.stream.challenge.domain.ChallengeParticipation;
import com.runnity.stream.challenge.domain.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

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
            @Param("statuses") List<ParticipationStatus> statuses
    );

    /**
     * 특정 챌린지에 진행 중 상태(IN_PROGRESS_STATUSES) 참가자가 있는지 확인
     */
    @Query("""
        SELECT COUNT(p) > 0
        FROM ChallengeParticipation p
        WHERE p.challengeId = :challengeId
          AND p.status IN :statuses
          AND p.isDeleted = false
    """)
    boolean existsByChallengeIdAndStatusIn(
            @Param("challengeId") Long challengeId,
            @Param("statuses") Set<ParticipationStatus> statuses
    );
}
