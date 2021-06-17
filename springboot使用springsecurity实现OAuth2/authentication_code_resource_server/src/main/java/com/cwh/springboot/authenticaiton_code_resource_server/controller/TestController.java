package com.cwh.springboot.authenticaiton_code_resource_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cwh
 * @date 2021/6/17 14:42
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public String test(){
        return "test success";
    }
}
