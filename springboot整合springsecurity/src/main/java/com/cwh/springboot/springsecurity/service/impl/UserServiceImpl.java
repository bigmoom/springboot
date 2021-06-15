package com.cwh.springboot.springsecurity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.cwh.springboot.springsecurity.config.security.JWTManager;
import com.cwh.springboot.springsecurity.config.security.UserDetail;
import com.cwh.springboot.springsecurity.dao.UserMapper;
import com.cwh.springboot.springsecurity.exception.MyException;
import com.cwh.springboot.springsecurity.model.entity.UserEntity;
import com.cwh.springboot.springsecurity.model.param.LoginParam;
import com.cwh.springboot.springsecurity.model.vo.UserVO;
import com.cwh.springboot.springsecurity.service.ResourceService;
import com.cwh.springboot.springsecurity.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**自定义的userDetailService
 * 实现loadUserByUsername()
 * @author cwh
 * @date 2021/6/11 17:37
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper,UserEntity> implements UserService,UserDetailsService{

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private JWTManager jwtManager;

    @Autowired
    private PasswordEncoder passwordEncoder;
    /**
     * 实现loadByUserName(),返回需要的userDetail
     * @param name
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
//      调用mapper获取用户对象
        UserEntity userEntity = baseMapper.selectByUserName(name);
//      如果没查到，抛出异常
        if(userEntity == null){
            throw new UsernameNotFoundException("用户没有找到");
        }
//      获取用户权限列表

        Set<SimpleGrantedAuthority> authorities = resourceService.getResourceByUserId(userEntity.getId())
                .stream()
                .map(String::valueOf)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
//      返回我们需要的额userDetail对象
        return new UserDetail(userEntity,authorities);
    }

    @Override
    public UserVO login(LoginParam param) {
//        根据用户名查询出用户实体
        UserEntity user = baseMapper.selectByUserName(param.getUsername());

//        若没有查到或者密码错误则抛出异常
        if(user ==null || !passwordEncoder.matches(param.getPassword(), user.getUserPassword())){
            throw new MyException("账号密码错误");
        }
        UserVO userVO = new UserVO();
        userVO.setId(user.getId())
                .setUsername(user.getUserName())
                .setToken(jwtManager.generate(user.getUserName()))
                .setResourceIds(resourceService.getResourceByUserId(user.getId()));
        return userVO;
    }
}
