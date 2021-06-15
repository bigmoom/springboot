package com.cwh.springboot.springsecurity.config.security;

import com.cwh.springboot.springsecurity.model.entity.UserEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * @author cwh
 * @date 2021/6/11 17:24
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class UserDetail extends User {

    private UserEntity userEntity;

    public UserDetail(UserEntity userEntity, Collection<? extends GrantedAuthority> authorities) {
//      调用父类的构造器方法，传入用户名，密码和权限列表
        super(userEntity.getUserName(),userEntity.getUserPassword(),authorities);
        this.userEntity = userEntity;
    }
}
