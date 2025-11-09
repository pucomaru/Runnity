package com.runnity.global.status;

import com.runnity.global.base.BaseErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorStatus {
    // 공통
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 401, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, 403, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, 404, "요청한 자원을 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 405, "허용되지 않은 메소드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 내부 오류입니다."),

    //auth
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 Refresh Token입니다."),

    // member
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, 404, "존재하지 않는 회원입니다."),
    NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, 400, "닉네임을 입력해주세요."),
    NICKNAME_FORMAT_INVALID(HttpStatus.BAD_REQUEST, 400, "닉네임 길이는 2~50자 입니다."),
    NICKNAME_CONFLICT(HttpStatus.BAD_REQUEST, 400, "사용할 수 없는 닉네임입니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 입력입니다."),


    // email
    EMAIL_FORMAT_INVALID(HttpStatus.BAD_REQUEST, 400, "올바르지 않은 이메일 형식입니다."),
    USER_EMAIL_FORBIDDEN(HttpStatus.FORBIDDEN, 403, "유저가 이메일에 대해 접근 권한이 없습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 이메일입니다."),

    // oauth
    OAUTH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "SNS로그인 오류입니다."),
    OAUTH_TOKEN_ERROR(HttpStatus.BAD_REQUEST, 400, "OAuth 토큰 발급에 실패했습니다."),
    PROFILE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "유저정보 불러오기 오류입니다."),
    USER_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "사용자 생성에 실패했습니다."),

    // challenge
    CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 챌린지입니다."),
    CHALLENGE_TIME_OVERLAP(HttpStatus.BAD_REQUEST, 400, "같은 시간대에 이미 참여 중인 챌린지가 있습니다."),

    //history
    RUN_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 운동 기록입니다."),
    RUN_RECORD_FORBIDDEN(HttpStatus.FORBIDDEN, 403, "본인의 운동 기록만 조회할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
