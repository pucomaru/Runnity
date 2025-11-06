package com.runnity.member.repository;

import com.runnity.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySocialUid(String socialUid);

    Optional<Member> findByEmail(String email);
}
