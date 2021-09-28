package com.zw.platform.service.leaderboard.impl;

import com.google.common.collect.Maps;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.leaderboard.CommonService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class CommonServiceImpl implements CommonService {
    private static final Logger log = LogManager.getLogger(CommonServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Override
    public Map<String, String> getUserOrg() {
        Map<String, String> userOrgMap = Maps.newHashMap();
        List<String> orgList = userService.getOrgIdsByUserDn(SystemHelper.getCurrentUser().getId().toString());
        // 所有组织
        List<OrganizationLdap> allGroup = organizationService.getAllOrganization();
        Map<String, String> allGroupMap = allGroup.stream().collect(
            Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
        orgList.forEach(groupId -> userOrgMap.put(groupId, allGroupMap.get(groupId)));
        return userOrgMap;
    }
}
