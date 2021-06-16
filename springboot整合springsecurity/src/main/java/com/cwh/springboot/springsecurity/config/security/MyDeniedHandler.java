package com.cwh.springboot.springsecurity.config.security;

import com.cwh.springboot.springsecurity.enums.ResultCode;
import com.cwh.springboot.springsecurity.model.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author cwh
 * @date 2021/6/16 10:36
 */
@Slf4j
public class MyDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest requestequest, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
//        写法与entrypoint类似
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        ResultVO<String> resultVO = new ResultVO<>(ResultCode.FORBIDDEN,"没有权限");
        out.write(resultVO.toString());
        out.flush();
        out.close();
    }
}
