package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

// 拦截RestController和Controller注解上的方法的Exception
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    // 具体拦截的异常为SQLIntegrityConstraintViolationException异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] messagePart = ex.getMessage().split(" ");  // 按照空格分隔开,然后取某个特定字段
            String uniqueKey = messagePart[2];
            String errorMsg = uniqueKey + " already exists.";
            return R.error(errorMsg);
        }
        return R.error("unknown error");
    }
}