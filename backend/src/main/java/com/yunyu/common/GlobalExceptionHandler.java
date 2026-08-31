package com.yunyu.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace();
        return Result.error("服务器内部错误: " + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }
}
