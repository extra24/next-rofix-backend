package com.rofix.fitspot.webservice.exception;

public class UserException extends BusinessException {
  public UserException(ErrorCode errorCode) {
    super(errorCode);
  }

  public UserException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }

  public static UserException userNotFound(Long userId) {
    return new UserException(ErrorCode.USER_NOT_FOUND, "사용자 ID " + userId + "를 찾을 수 없습니다.");
  }

  public static UserException userNotFound(String email) {
    return new UserException(ErrorCode.USER_NOT_FOUND, "이메일 " + email + "로 등록된 사용자를 찾을 수 없습니다.");
  }

  public static UserException userAlreadyExists(String email) {
    return new UserException(ErrorCode.USER_EMAIL_DUPLICATE, "이메일 " + email + "는 이미 사용 중입니다.");
  }

  public static UserException invalidPassword() {
    return new UserException(ErrorCode.USER_INVALID_PASSWORD);
  }

  public static UserException accountLocked() {
    return new UserException(ErrorCode.USER_ACCOUNT_LOCKED);
  }
}
