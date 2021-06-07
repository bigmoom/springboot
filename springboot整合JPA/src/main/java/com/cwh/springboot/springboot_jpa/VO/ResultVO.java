package com.cwh.springboot.springboot_jpa.VO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author cwh
 * @date 2021/6/7 16:47
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVO<T> {

    private Integer code;

    private String msg;

    private T data;


}
