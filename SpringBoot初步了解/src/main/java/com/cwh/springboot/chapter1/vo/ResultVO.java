package com.cwh.springboot.chapter1.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author cwh
 * @date 2021/6/7 14:04
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVO<T> {

    private Integer code;

    private String msg;

    private T data;

    public ResultVO(){}

    public ResultVO(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }
}


