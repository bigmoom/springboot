package com.cwh.springboot.springsession.controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cwh
 * @date 2021/6/22 9:25
 */
@RestController
@RequestMapping("/session")
public class SessionController {

    /**
     * 设置session
     * @param request
     * @param attributes
     * @return
     */
    @PostMapping("/set")
    public Map<String,Object> setSession(HttpServletRequest request, @RequestParam("attributes")String attributes){
        request.getSession().setAttribute("attributes",attributes);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("SessionID:",request.getSession().getId());
        return map;
    }

    /**
     * 获取session
     * @param request
     * @return
     */
    @GetMapping("/get")
    public String getSession(HttpServletRequest request){
        String attributes = (String) request.getSession().getAttribute("attributes");
        return attributes;
    }
}
