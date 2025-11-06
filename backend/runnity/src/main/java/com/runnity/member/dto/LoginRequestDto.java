package com.runnity.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    // Getter, Setter, 생성자 등이 필요합니다 (Lombok을 썼다면 @Getter, @Setter만 추가)
    private String provider;
    private String idToken;
}
