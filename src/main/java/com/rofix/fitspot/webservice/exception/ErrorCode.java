package com.rofix.fitspot.webservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통 에러 (1000번대)
    INVALID_INPUT_VALUE(1000, "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(1001, "지원하지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(1002, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(1003, "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(1004, "잘못된 타입의 값입니다."),
    HANDLE_ACCESS_DENIED(1005, "접근이 거부되었습니다."),

    // 사용자 관련 에러 (2000번대)
    USER_NOT_FOUND(2000, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(2001, "이미 존재하는 사용자입니다."),
    USER_EMAIL_DUPLICATE(2002, "이미 사용 중인 이메일입니다."),
    USER_INVALID_PASSWORD(2003, "잘못된 비밀번호입니다."),
    USER_ACCOUNT_LOCKED(2004, "계정이 잠겨있습니다."),

    // 인증/인가 관련 에러 (3000번대)
    UNAUTHORIZED(3000, "인증이 필요합니다."),
    FORBIDDEN(3001, "권한이 없습니다."),
    TOKEN_EXPIRED(3002, "토큰이 만료되었습니다."),
    INVALID_TOKEN(3003, "유효하지 않은 토큰입니다."),

    // 비즈니스 로직 에러 (4000번대)
    BUSINESS_RULE_VIOLATION(4000, "비즈니스 규칙을 위반했습니다."),
    INSUFFICIENT_BALANCE(4001, "잔액이 부족합니다."),
    ORDER_NOT_FOUND(4002, "주문을 찾을 수 없습니다."),

    // 외부 서비스 에러 (5000번대)
    EXTERNAL_SERVICE_ERROR(5000, "외부 서비스 오류가 발생했습니다."),
    EXTERNAL_SERVICE_TIMEOUT(5001, "외부 서비스 응답 시간이 초과되었습니다.");

    private final int code;
    private final String message;
}
