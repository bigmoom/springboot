package com.cwh.springboot.springsecurity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cwh.springboot.springsecurity.model.entity.UserEntity;
import com.cwh.springboot.springsecurity.model.param.LoginParam;
import com.cwh.springboot.springsecurity.model.vo.UserVO;
import org.springframework.security.core.userdetails.User;

/**
 * @author cwh
 * @date 2021/6/11 17:35
 */
public interface UserService extends IService<UserEntity> {

    /**
     * 登录
     * @param user 登录参数
     * @return  成功则返回UserVO，失败抛出异常
     */
    UserVO login(LoginParam user);
}
