package com.zw.platform.service.oilsubsidy.impl;

import com.zw.platform.domain.oilsubsidy.subsidyManage.SubsidyManageResp;
import com.zw.platform.repository.vas.ForwardVehicleManageDao;
import com.zw.platform.service.oilsubsidy.SubsidyManageService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: tianzhangxu
 * @Description: 补传管理serviceImpl
 * @Date: 2021/3/25 17:23
 */
@Service
public class SubsidyManageServiceImpl implements SubsidyManageService {

    @Autowired
    private ForwardVehicleManageDao forwardVehicleManageDao;


    @Override
    public List<SubsidyManageResp> getDetail(String orgIds) {
        List<String> orgIdList = Arrays.asList(orgIds.split(","));
        if (CollectionUtils.isEmpty(orgIdList)) {
            return new ArrayList<>();
        }
        return forwardVehicleManageDao.getForwardVehicleByOrgIds(orgIdList);
    }
}