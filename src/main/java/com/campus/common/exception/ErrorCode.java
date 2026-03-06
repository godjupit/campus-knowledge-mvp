package com.campus.common.exception;

public enum ErrorCode {
    USERNAME_EXISTS(10001, "用户名已存在", 409),
    EMAIL_EXISTS(10002, "邮箱已存在", 409),
    PARAM_INVALID(10003, "参数错误", 400),
    SYSTEM_ERROR(99999, "系统异常", 500);

    private final int code;
    private final String message;
    private final int httpStatus;

    ErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
