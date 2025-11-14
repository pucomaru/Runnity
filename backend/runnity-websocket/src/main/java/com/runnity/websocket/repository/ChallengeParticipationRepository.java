package com.runnity.websocket.repository;

import com.runnity.websocket.domain.ChallengeParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
    
    /**
     * challengeId와 memberId로 참가 정보 조회
     * 
     * @param challengeId 챌린지 ID
     * @param memberId 회원 ID
     * @return 참가 정보
     */
    Optional<ChallengeParticipation> findByChallengeIdAndMemberId(Long challengeId, Long memberId);
}

