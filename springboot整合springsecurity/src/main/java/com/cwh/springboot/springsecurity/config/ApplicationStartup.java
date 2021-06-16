package com.cwh.springboot.springsecurity.config;

import com.cwh.springboot.springsecurity.annotation.Auth;
import com.cwh.springboot.springsecurity.config.security.MySecurityMetadataSource;
import com.cwh.springboot.springsecurity.model.entity.Resource;
import com.cwh.springboot.springsecurity.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**用于启动前根据接口更新权限列表
 * @author cwh
 * @date 2021/6/16 10:53
 */
@Component
public class ApplicationStartup implements ApplicationRunner {
//   用于获取接口信息
    @Autowired
   private RequestMappingInfoHandlerMapping requestMappingInfoHandlerMapping;

    @Autowired
    private ResourceService resourceService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Resource> list = getAuthResource();

        if(list.isEmpty()){
            return;
        }

//        权限放到缓存中
        MySecurityMetadataSource.getRESOURCES().addAll(list);
//        将资源数据批量添加到数据库中
        resourceService.updateResources(list);
    }

    private List<Resource> getAuthResource(){

        List<Resource> list = new LinkedList<>();

//      获取接口信息
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = requestMappingInfoHandlerMapping.getHandlerMethods();
        handlerMethodMap.forEach((info,handlerMethod) ->{
//          获取类上的权限值
            Auth moduleAuth = handlerMethod.getBeanType().getAnnotation(Auth.class);
//          获取方法上的权限值
            Auth methodAuth = handlerMethod.getMethod().getAnnotation(Auth.class);

            if(moduleAuth == null || methodAuth == null){
                return;
            }
//          获取接口请求方法（GET/POST）
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
//          生成url
            String path = methods.toArray()[0]+":" + info.getPatternsCondition().getPatterns().toArray()[0];

            Resource resource = new Resource();
            resource.setUrl(path);
//          设置权限值为类权限值加方法权限值
            resource.setId(moduleAuth.id()+ methodAuth.id());
            list.add(resource);

        });
        return list;
    }
}
