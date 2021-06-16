package com.cwh.springboot.springsecurity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cwh.springboot.springsecurity.dao.ResourceMapper;
import com.cwh.springboot.springsecurity.model.entity.Resource;
import com.cwh.springboot.springsecurity.service.ResourceService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * @author cwh
 * @date 2021/6/15 9:02
 */
@Service

public class ResourceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {


    @Override
    public Set<Long> getResourceByUserId(Long userId) {
        return baseMapper.selectResourceIdByUserId(userId);
    }

    @Override
    public void updateResources(Collection<Resource> resources) {
            baseMapper.updateResources(resources);
    }
}
