package com.cwh.springboot.springsecurity.controller;

import com.cwh.springboot.springsecurity.annotation.Auth;
import com.cwh.springboot.springsecurity.model.param.LoginParam;
import com.cwh.springboot.springsecurity.model.vo.ResultVO;
import com.cwh.springboot.springsecurity.model.vo.UserVO;
import com.cwh.springboot.springsecurity.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author cwh
 * @date 2021/6/15 16:49
 */
@RestController
@Slf4j
@RequestMapping("/user")
@Auth(id=1000 , name="用户操作")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping()
    @Auth(id=1,name="获取用户信息")
    public String getUserList() {
        return "test";
    }

    @PostMapping
    @Auth(id=2,name="添加用户信息")
    public String addUser(@RequestBody @Validated LoginParam user){
        userService.createUser(user);
        return "添加成功";
    }
}
