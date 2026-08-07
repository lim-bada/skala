package com.skala.shop.exception;

public class ParameterException extends BusinessException {

    public ParameterException(String message) {
        super(ErrorCode.INVALID_PARAMETER, message);
    }
}
