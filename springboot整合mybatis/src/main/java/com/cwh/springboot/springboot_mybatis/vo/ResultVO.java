package com.cwh.springboot.springboot_mybatis.vo;

import com.cwh.springboot.springboot_mybatis.enums.ResultCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author cwh
 * @date 2021/6/8 14:00
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVO<T> {

    private Integer code;
    private String msg;
    private T data;

    public ResultVO(){
        this(ResultCode.SUCCESS, null);
    }
    public ResultVO(T data) {
        this(ResultCode.SUCCESS, data);
    }

    public ResultVO(ResultCode resultCode,T data){
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
        this.data = data;
    }
}
