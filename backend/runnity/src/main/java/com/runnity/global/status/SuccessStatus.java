package com.runnity.global.status;

import com.runnity.global.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseSuccessStatus {
    // 공통
    OK(HttpStatus.OK, 200, "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, 201, "생성이 완료되었습니다."),

    // auth
    LOGIN_SUCCESS(HttpStatus.OK, 200, "로그인이 성공적으로 완료되었습니다."),

    //member
    PROFILE_FETCH_OK(HttpStatus.OK, 200, "프로필 조회 성공"),
    PROFILE_UPDATE_OK(HttpStatus.OK, 200, "프로필 수정 성공"),
    NICKNAME_CHECK_OK(HttpStatus.OK, 200, "닉네임 검사 성공"),

    // challenge
    CHALLENGE_CREATED(HttpStatus.CREATED, 201, "챌린지가 성공적으로 생성되었습니다."),
    CHALLENGE_JOINED(HttpStatus.CREATED, 201, "챌린지 참가 신청이 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
