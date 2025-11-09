package com.runnity.member.repository;

import com.runnity.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySocialUid(String socialUid);
    Optional<Member> findByEmail(String email);

    @Query("SELECT (COUNT(m)>0) " +
            "FROM Member m " +
            "WHERE LOWER(TRIM(m.nickname)) = LOWER(TRIM(:nickname)) " +
            "AND (:memberId IS NULL OR m.id <> :memberId)")
    boolean nicknameCheckWithMemberId(@Param("nickname") String nickname, @Param("memberId") Long memberId);


}
