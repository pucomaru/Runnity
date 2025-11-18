package com.runnity.notification.domain;

import com.runnity.global.domain.BaseEntity;
import com.runnity.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_fcm_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberFcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fcm_token_id")
    private Long fcmTokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_member_fcm_token_member"))
    private Member member;

    @Column(nullable = false, length = 255)
    private String token;


    @Builder
    MemberFcmToken(Member member, String token){
        this.member = member;
        this.token = token;
    }
}
