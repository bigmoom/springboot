package com.cwh.springboot.springsecurity.controller;

import com.cwh.springboot.springsecurity.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cwh
 * @date 2021/6/15 16:49
 */
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {


    @GetMapping("/get")
    public String getUserList(){
        return "test";
    }
}
