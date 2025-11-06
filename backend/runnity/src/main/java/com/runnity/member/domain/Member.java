package com.runnity.member.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "social_uid", nullable = false, unique = true)
    private String socialUid; // 소셜 로그인 고유 ID

    private String socialType;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "height")
    private Float height;

    @Column(name = "weight")
    private Float weight;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birth")
    private String birth;

    @Column(name = "average_pace")
    private Integer averagePace;

    // 추가 정보 업데이트 메서드
    public void updateProfile(String nickname, Float height, Float weight, String gender, String birth) {
        this.nickname = nickname;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.birth = birth;
    }

    public void updateProfilePartially(String nickname, Float height, Float weight) {
        if (nickname != null && !nickname.isEmpty()) {
            this.nickname = nickname;
        }
        if (height > 0) {
            this.height = height;
        }
        if (weight > 0) {
            this.weight = weight;
        }
    }
}
