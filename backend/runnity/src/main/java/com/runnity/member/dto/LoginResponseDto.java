package com.runnity.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;

    public LoginResponseDto(String accessToken, String refreshToken, boolean isNewUser) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.isNewUser = isNewUser;
    }

    public String getRefreshToken() { return refreshToken; }
    public boolean getIsNewUser() { return isNewUser; }
}
