package com.cwh.springboot.springsecurity.exception;

import com.cwh.springboot.springsecurity.enums.ResultCode;
import lombok.Getter;

/**
 * @author cwh
 * @date 2021/6/15 11:30
 */
@Getter
public class MyException extends  RuntimeException{
    private final String msg;
    private final ResultCode resultCode;

    public MyException() {
        this(ResultCode.FAILED);
    }

    public MyException(String msg) {
        this(ResultCode.FAILED, msg);
    }

    public MyException(ResultCode resultCode) {
        this(resultCode, resultCode.getMsg());
    }

    public MyException(ResultCode resultCode, String msg) {
        super(msg);
        this.resultCode = resultCode;
        this.msg =  msg;
    }
}
