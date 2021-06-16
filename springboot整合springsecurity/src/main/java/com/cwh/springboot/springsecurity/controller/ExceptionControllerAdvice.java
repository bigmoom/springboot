package com.cwh.springboot.springsecurity.controller;

import com.cwh.springboot.springsecurity.exception.MyException;
import com.cwh.springboot.springsecurity.model.vo.ResultVO;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author cwh
 * @date 2021/6/16 10:39
 */
@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(MyException.class)
    public ResultVO<String> myExceptionHandler(MyException e){
        return new ResultVO<>(e.getResultCode(),e.getMsg());
    }
}
