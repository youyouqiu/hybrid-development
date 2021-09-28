package com.zw.platform.basic.service.impl;

import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.OrganizationGroupDO;
import com.zw.platform.basic.repository.OrganizationGroupDao;
import com.zw.platform.basic.service.OrganizationGroupService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author wanxing
 * @Title: 组织-分组service类
 * @date 2020/12/1617:37
 */
@Service
public class OrganizationGroupServiceImpl implements OrganizationGroupService {
    private static final Logger log = LogManager.getLogger(OrganizationGroupServiceImpl.class);
    @Autowired
    private OrganizationGroupDao organizationGroupDao;

    @Override
    public void initCache() {
        log.info("开始进行组织-分组的redis初始化.");
        //初始化组织-分组的缓存
        List<OrganizationGroupDO> allData = getAll();
        RedisHelper.delByPattern(RedisKeyEnum.ORG_GROUP_PATTERN.of());
        if (allData.isEmpty()) {
            return;
        }
        Map<RedisKey, Collection<String>> map = new HashMap<>(allData.size() * 2);
        for (OrganizationGroupDO organizationGroupDO : allData) {
            map.computeIfAbsent(RedisKeyEnum.ORG_GROUP.of(organizationGroupDO.getOrgId()), o -> new HashSet<>())
                .add(organizationGroupDO.getGroupId());
        }
        RedisHelper.batchAddToSet(map);
        log.info("结束组织-分组的redis初始化.");
    }

    @Override
    public List<OrganizationGroupDO> getAll() {
        return organizationGroupDao.getAll();
    }
}
