package com.cwh.springboot.springsecurity.controller;

import com.cwh.springboot.springsecurity.model.param.LoginParam;
import com.cwh.springboot.springsecurity.model.vo.UserVO;
import com.cwh.springboot.springsecurity.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cwh
 * @date 2021/6/15 11:15
 */
@RestController
@Slf4j
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public UserVO login(@RequestBody @Validated LoginParam user){

        return userService.login(user);
    }

}
