package com.skala.shop.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "요청값이 올바르지 않습니다"),
    NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "고객 ID 또는 비밀번호가 올바르지 않습니다"),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 데이터를 찾을 수 없습니다"),
    DATA_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 데이터입니다"),
    DATA_IN_USE(HttpStatus.CONFLICT, "사용 중인 데이터는 삭제할 수 없습니다"),
    INSUFFICIENT_FUNDS(HttpStatus.BAD_REQUEST, "상품을 주문할 포인트가 부족합니다"),
    INSUFFICIENT_QUANTITY(HttpStatus.BAD_REQUEST, "주문한 수량보다 많이 취소할 수 없습니다"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "상품 재고가 부족합니다"),
    CONCURRENT_ORDER_CONFLICT(HttpStatus.CONFLICT, "동시 주문이 발생했습니다. 다시 시도해 주세요"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다");

    private final HttpStatus status;
    private final String message;
}
