package com.tiger.apigateway.exceptions;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthLogicException extends RuntimeException {

    private final ErrorCode errorCode;

    public AuthLogicException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
