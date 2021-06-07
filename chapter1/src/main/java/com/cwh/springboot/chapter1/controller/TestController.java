package com.cwh.springboot.chapter1.controller;

import com.cwh.springboot.chapter1.vo.ResultVO;
import org.springframework.web.bind.annotation.*;

/**
 * @author cwh
 * @date 2021/6/7 10:53
 */
@RestController
public class TestController {

    @GetMapping("/getmessage")
    public String getMessage(){
        return "getMessage success";
    }

    @PostMapping("/addmessage")
    public String addmessage(@RequestParam(value="name",required = true)String name,@RequestParam("age") Integer age) {
        return "name:" + name + " age:" + age;
    }

    @PostMapping("/addmessage/{name}")
    public String addmessage(@PathVariable("name")String name) {
        return "name:" + name;
    }

    @GetMapping("/addmessage")
    public ResultVO<String> getmessage(@RequestParam(value="name",required = true)String name ){
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(1);
        resultVO.setMsg("success");
        resultVO.setData("name:"+name);
        return resultVO;
    }
}
