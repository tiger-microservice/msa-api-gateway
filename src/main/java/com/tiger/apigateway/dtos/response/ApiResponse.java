package com.tiger.apigateway.dtos.response;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {

    private int status;
    private T data;
    private String message;

    public static <T> ApiResponse<T> responseOK(T data) {
        return ApiResponse.<T>builder().status(HttpStatus.OK.value()).data(data).build();
    }

    public static <T> ApiResponse<T> responseOK() {
        return ApiResponse.<T>builder().status(HttpStatus.OK.value()).build();
    }

    public static <T> ApiResponse<T> responseError(int code, String message) {
        return ApiResponse.<T>builder().status(code).message(message).build();
    }
}
