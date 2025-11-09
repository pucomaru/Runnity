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

    // member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 회원입니다."),

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
    CHALLENGE_ALREADY_JOINED(HttpStatus.BAD_REQUEST, 400, "이미 참가 중인 챌린지입니다."),
    CHALLENGE_NOT_JOINED(HttpStatus.BAD_REQUEST, 400, "참가하지 않은 챌린지입니다."),
    CHALLENGE_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 400, "대기 중인 상태에서만 참가 취소할 수 있습니다."),
    CHALLENGE_REJOIN_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 400, "참가 취소한 상태에서만 재참가할 수 있습니다."),
    CHALLENGE_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, 400, "비밀번호가 일치하지 않습니다."),
    CHALLENGE_NOT_RECRUITING(HttpStatus.FORBIDDEN, 403, "모집 중이 아닌 챌린지입니다."),
    CHALLENGE_PARTICIPANT_LIMIT_EXCEEDED(HttpStatus.FORBIDDEN, 403, "참가 인원이 가득 찼습니다."),

    //history
    RUN_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 운동 기록입니다."),
    RUN_RECORD_FORBIDDEN(HttpStatus.FORBIDDEN, 403, "본인의 운동 기록만 조회할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
