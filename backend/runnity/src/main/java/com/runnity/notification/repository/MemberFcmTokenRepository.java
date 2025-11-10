package com.runnity.notification.repository;

import com.runnity.notification.domain.MemberFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberFcmTokenRepository extends JpaRepository<MemberFcmToken, Long> {

    Optional<MemberFcmToken> findByMember_MemberIdAndTokenAndIsDeletedFalse(Long memberId, String token);


    Optional<MemberFcmToken> findByToken(String token);

}
