package com.spaceres.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Auth
    DUPLICATE_EMAIL(400, "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 Refresh Token입니다."),
    EXPIRED_REFRESH_TOKEN(401, "만료된 Refresh Token입니다. 다시 로그인해주세요."),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),

    // Space
    SPACE_NOT_FOUND(404, "공간을 찾을 수 없습니다."),
    SPACE_UNAVAILABLE(400, "현재 사용할 수 없는 공간입니다."),

    // Reservation
    RESERVATION_NOT_FOUND(404, "예약을 찾을 수 없습니다."),
    RESERVATION_CONFLICT(409, "해당 시간대에 이미 예약이 있습니다."),
    INVALID_TIME_RANGE(400, "종료 시간은 시작 시간 이후여야 합니다."),
    EXCEEDS_MAX_DURATION(400, "1회 최대 예약 시간은 2시간입니다."),
    OUTSIDE_OPERATING_HOURS(400, "예약 가능 시간은 오전 9시 ~ 오후 9시입니다."),
    ALREADY_CANCELLED(400, "이미 취소된 예약입니다."),
    FORBIDDEN_ACCESS(403, "본인의 예약만 취소할 수 있습니다."),

    // Common
    INVALID_INPUT(400, "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(500, "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String message;
}
