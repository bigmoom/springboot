package com.cwh.springboot.springsecurity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cwh.springboot.springsecurity.model.entity.Resource;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author cwh
 * @date 2021/6/15 9:01
 */
public interface ResourceService extends IService<Resource> {

    Set<Long> getResourceByUserId(Long userId);

    void updateResources(Collection<Resource> resources);
}
