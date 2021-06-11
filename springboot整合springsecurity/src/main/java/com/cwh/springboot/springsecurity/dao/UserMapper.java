package com.cwh.springboot.springsecurity.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cwh.springboot.springsecurity.model.entity.UserEntity;
import org.springframework.stereotype.Repository;

/**
 * @author cwh
 * @date 2021/6/11 17:40
 */
@Repository
public interface UserMapper extends BaseMapper<UserEntity> {

    public UserEntity selectByUserName(String name);

}
