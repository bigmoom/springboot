package com.cwh.springboot.springboot_mybatisplus.vo;

import com.cwh.springboot.springboot_mybatisplus.enums.ResultCode;
import lombok.Getter;

/**
 * @author cwh
 * @date 2021/6/9 14:59
 */
@Getter
public class ResultVO<T> {

    /**
     * 状态码, 默认1000是成功
     */
    private int code;
    /**
     * 响应信息, 来说明响应情况
     */
    private String msg;
    /**
     * 响应的具体数据
     */
    private T data;

    public ResultVO(T data) {
        this(ResultCode.SUCCESS, data);
    }

    public ResultVO(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":\"%s\"}", code, msg, data.toString());
    }
}
