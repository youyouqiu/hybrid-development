package com.zw.platform.basic.repository;

import com.zw.platform.basic.dto.OrganizationGroupDO;

import java.util.List;

/**
 * @author wanxing
 * @Title: 组织分组Dao
 * @date 2020/12/1617:40
 */
public interface OrganizationGroupDao {
    /**
     * 获取所有组织-分组关系
     * @return
     */
    List<OrganizationGroupDO> getAll();

}
