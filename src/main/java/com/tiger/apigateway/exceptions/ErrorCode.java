package com.tiger.apigateway.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_KEY(400, "MSG00001", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(404, "MSG00002", HttpStatus.NOT_FOUND),
    BEAN_NOT_DEFINED(404, "MSG00003", HttpStatus.NOT_FOUND),

    USER_NOT_EXISTED(404, "MSG00004", HttpStatus.FORBIDDEN),
    USERNAME_INVALID(403, "MSG00005", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(401, "MSG00006", HttpStatus.UNAUTHORIZED),
    UNCATEGORIZED_EXCEPTION(500, "MSG00007", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_INVALID(400, "MSG00008", HttpStatus.BAD_REQUEST),
    EMAIL_FORMAT_INVALID(400, "MSG00009", HttpStatus.BAD_REQUEST),
    EMAIL_EMPTY_INVALID(400, "MSG00010", HttpStatus.BAD_REQUEST),
    PASSWORD_MAXLENGTH_INVALID(400, "MSG00011", HttpStatus.BAD_REQUEST),

    // permission
    PERMISSION_CODE_INVALID(400, "MSG00012", HttpStatus.BAD_REQUEST),
    PERMISSION_NAME_MAXLENGTH_INVALID(400, "MSG00013", HttpStatus.BAD_REQUEST),
    PERMISSION_DESCRIPTION_MAXLENGTH_INVALID(400, "MSG00014", HttpStatus.BAD_REQUEST),

    // role
    ROLE_NAME_MAXLENGTH_INVALID(400, "MSG00015", HttpStatus.BAD_REQUEST),
    ROLE_DESCRIPTION_MAXLENGTH_INVALID(400, "MSG00016", HttpStatus.BAD_REQUEST),
    ROLE_CODE_INVALID(400, "MSG00017", HttpStatus.BAD_REQUEST),

    // account user
    USER_EXIST_INVALID(400, "MSG00018", HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
