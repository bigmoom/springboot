package com.cwh.springboot.springsecurity.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cwh.springboot.springsecurity.model.entity.Resource;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

/**
 * @author cwh
 * @date 2021/6/11 18:01
 */
@Repository
public interface ResourceMapper extends BaseMapper<Resource> {

    Set<Long> selectResourceIdByUserId(Long userId);

    int updateResources(@Param("resources")Collection<Resource> resources);
}
