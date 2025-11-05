package com.runnity.global.status;

import com.runnity.global.base.BaseSuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseSuccessStatus {

    OK(HttpStatus.OK, 200, "요청이 성공적으로 처리되었습니다."),
    CHAT_SUCCESS(HttpStatus.OK, 200, "AI와의 대화가 성공적으로 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, 200, "로그인이 성공적으로 완료되었습니다."),

    GET_EMAIL_PERIOD_SUCCESS(HttpStatus.OK, 200, "이메일 전송 주기 조회가 성공적으로 완료되었습니다."),
    PATCH_EMAIL_PERIOD_SUCCESS(HttpStatus.OK, 200, "이메일 전송 주기 수정이 성공적으로 완료되었습니다."),

    GET_EMAIL_LIST_SUCCESS(HttpStatus.OK, 200, "이메일 리스트 조회가 성공적으로 완료되었습니다."),
    POST_EMAIL_SUCCESS(HttpStatus.OK, 200, "이메일 추가가 성공적으로 완료되었습니다."),
    PATCH_EMAIL_SUCCESS(HttpStatus.OK, 200, "이메일 수정이 성공적으로 완료되었습니다."),
    DELETE_EMAIL_SUCCESS(HttpStatus.OK, 200, "이메일 삭제가 성공적으로 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;
}
