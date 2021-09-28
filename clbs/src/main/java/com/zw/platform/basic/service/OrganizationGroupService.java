package com.zw.platform.basic.service;

import com.zw.platform.basic.dto.OrganizationGroupDO;

import java.util.List;

/**
 * @author wanxing
 * @Title: 组织-分组service
 * @date 2020/12/1617:36
 */
public interface OrganizationGroupService extends CacheService {

    /**
     * 获取所有组织-分组关系
     * @return
     */
    List<OrganizationGroupDO> getAll();
}
