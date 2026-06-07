package com.unispace.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),

    // 공간
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "공간을 찾을 수 없습니다."),
    ROOM_INACTIVE(HttpStatus.BAD_REQUEST, "현재 예약할 수 없는 공간입니다."),
    FACILITY_NOT_FOUND(HttpStatus.NOT_FOUND, "시설을 찾을 수 없습니다."),

    // 예약
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    RESERVATION_CONFLICT(HttpStatus.CONFLICT, "해당 시간대에 이미 예약이 존재합니다."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "예약 시간이 올바르지 않습니다."),
    RESERVATION_NOT_OWNED(HttpStatus.FORBIDDEN, "본인의 예약만 처리할 수 있습니다."),
    ALREADY_RETURNED(HttpStatus.BAD_REQUEST, "이미 반납/취소된 예약입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
