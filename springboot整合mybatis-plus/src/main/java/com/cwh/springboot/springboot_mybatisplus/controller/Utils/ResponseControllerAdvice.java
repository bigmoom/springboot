package com.cwh.springboot.springboot_mybatisplus.controller.Utils;

import com.baomidou.mybatisplus.extension.exceptions.ApiException;
import com.cwh.springboot.springboot_mybatisplus.vo.ResultVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**全局统一相应，以控制返回数据格式
 * 扫描controller包
 * @author cwh
 * @date 2021/6/9 14:57
 */
@RestControllerAdvice(basePackages = {"com.cwh.springboot.springboot_mybatisplus.controller"})
public class ResponseControllerAdvice  implements ResponseBodyAdvice<Object> {
    /**
     * 判断是否调用beforeBodyWrite,false为不调用
     * @param returnType
     * @param aClass
     * @return
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> aClass) {
//        若返回类型已经是ResultVO,则不调用
        return !returnType.getParameterType().equals(ResultVO.class);

    }

    @Override
    public Object beforeBodyWrite(Object data, MethodParameter returnType, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        // String类型不能直接包装，所以要进行些特别的处理
        if (returnType.getGenericParameterType().equals(String.class)) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 将数据包装在ResultVO里后，再转换为json字符串响应给前端
                return objectMapper.writeValueAsString(new ResultVO<>(data));
            } catch (JsonProcessingException e) {
                throw new ApiException("返回String类型错误");
            }
        }
        // 将原本的数据包装在ResultVO里
        return new ResultVO<>(data);
    }

}
