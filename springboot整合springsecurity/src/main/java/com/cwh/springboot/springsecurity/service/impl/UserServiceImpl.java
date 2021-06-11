package com.cwh.springboot.springsecurity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.cwh.springboot.springsecurity.dao.UserMapper;
import com.cwh.springboot.springsecurity.model.entity.UserEntity;
import com.cwh.springboot.springsecurity.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**自定义的userDetailService
 * 实现loadUserByUsername()
 * @author cwh
 * @date 2021/6/11 17:37
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,UserEntity> implements UserService,UserDetailsService{


    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
//      调用mapper获取用户对象
        UserEntity userEntity = baseMapper.selectByUserName(name);
//      如果没查到，抛出异常
        if(userEntity == null){
            throw new UsernameNotFoundException("用户没有找到");
        }
//      获取用户权限列表


    }



}
