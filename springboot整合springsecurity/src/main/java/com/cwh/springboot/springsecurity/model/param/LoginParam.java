package com.cwh.springboot.springsecurity.model.param;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * @author cwh
 * @date 2021/6/15 11:19
 */
@Data
public class LoginParam {
    @NotBlank(message = "用户名不能为空")
//    @Length(min = 4, max = 12, message = "用户名长度为4-12位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(min = 4, max = 12, message = "密码长度为4-12位")
    private String password;
}
