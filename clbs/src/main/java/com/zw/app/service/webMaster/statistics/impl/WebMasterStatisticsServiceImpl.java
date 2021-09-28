package com.zw.app.service.webMaster.statistics.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.webMaster.alarmType.ReferenceGroup;
import com.zw.app.domain.webMaster.statistics.StatisticsConfig;
import com.zw.app.domain.webMaster.statistics.StatisticsConfigInfo;
import com.zw.app.repository.mysql.webMaster.Statistics.StatisticsDao;
import com.zw.app.service.webMaster.statistics.WebMasterStatisticsService;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author lijie
 * @date 2018/12/07 17:19
 */
@Service
public class WebMasterStatisticsServiceImpl implements WebMasterStatisticsService {

    @Autowired
    StatisticsDao statisticsDao;
    @Autowired
    UserService userService;
    @Autowired
    OrganizationService organizationService;

    @Override
    public JSONObject getStatistics(String groupId) {
        JSONObject jsonObject = new JSONObject();
        if (!AppParamCheckUtil.check64String(groupId)) {
            return null;
        }
        String groupName;
        try {
            groupName = organizationService.getOrganizationByUuid(groupId).getName();
        } catch (Exception e) {
            return null;
        }
        List<StatisticsConfigInfo> statisticsConfigInfos = null;
        //通过组织id查询配置信息，id为参考组织id或当前组织id
        statisticsConfigInfos = statisticsDao.getStatistics(groupId, 0);
        List<StatisticsConfig> statisticsConfigs = new ArrayList<>();
        if (!statisticsConfigInfos.isEmpty()) {
            for (StatisticsConfigInfo statisticsConfigInfo : statisticsConfigInfos) {
                StatisticsConfig statisticsConfig = new StatisticsConfig();
                statisticsConfig.setName(statisticsConfigInfo.getName());
                statisticsConfig.setNumber(statisticsConfigInfo.getNumber());
                statisticsConfigs.add(statisticsConfig);
            }
        }
        jsonObject.put("statistics", JSON.toJSON(statisticsConfigs));
        jsonObject.put("orgId", groupId);
        jsonObject.put("orgName", groupName);
        return jsonObject;
    }

    @Override
    public Boolean updateStatisticsConfig(JSONArray jsonArray, String groupId) {
        if (!AppParamCheckUtil.check64String(groupId)) {
            return false;
        }
        String userName = SystemHelper.getCurrentUser().getUsername();
        String groupName;
        try {
            groupName = organizationService.getOrganizationByUuid(groupId).getName();
        } catch (Exception e) {
            return false;
        }
        Boolean success = true;
        List<StatisticsConfigInfo> statisticsConfigInfos = statisticsDao.getStatistics(groupId, 0);
        if (!statisticsConfigInfos.isEmpty()) {
            statisticsDao.deleteStatisticsConfig(groupId, 0);
        }
        List<StatisticsConfigInfo> updateStatisticsConfigInfos = new ArrayList<>();
        if (jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                StatisticsConfigInfo statisticsConfigInfo = new StatisticsConfigInfo();
                statisticsConfigInfo.setId(UUID.randomUUID().toString());
                statisticsConfigInfo.setUpdateDataUsername(userName);
                statisticsConfigInfo.setUpdateDataTime(new Date(new Date().getTime() + 1000 * i));
                statisticsConfigInfo.setGroupDefault(0);
                statisticsConfigInfo.setGroupId(groupId);
                statisticsConfigInfo.setGroupName(groupName);
                statisticsConfigInfo.setName(jsonArray.getJSONObject(i).getString("name"));
                statisticsConfigInfo.setNumber(jsonArray.getJSONObject(i).getInteger("number"));
                statisticsConfigInfo.setAppVersion(jsonArray.getJSONObject(i).getInteger("appVersion"));
                updateStatisticsConfigInfos.add(statisticsConfigInfo);
            }
            success = statisticsDao.addStatisticsConfig(updateStatisticsConfigInfos);
        }
        return success;
    }

    @Override
    public Boolean resetStatisticsConfig() {
        String userName = SystemHelper.getCurrentUser().getUsername();
        String groupId = userService.getCurrentUserOrg().getUuid();
        Boolean success = true;
        List<StatisticsConfigInfo> defaultStatisticsConfigInfos = statisticsDao.getStatistics(groupId, 1);
        statisticsDao.deleteStatisticsConfig(groupId, 0);
        if (!defaultStatisticsConfigInfos.isEmpty()) {
            for (StatisticsConfigInfo defaultStatisticsConfigInfo : defaultStatisticsConfigInfos) {
                defaultStatisticsConfigInfo.setId(UUID.randomUUID().toString());
                defaultStatisticsConfigInfo.setGroupDefault(0);
                defaultStatisticsConfigInfo.setUpdateDataTime(new Date());
                defaultStatisticsConfigInfo.setUpdateDataUsername(userName);
            }
            success = statisticsDao.addStatisticsConfig(defaultStatisticsConfigInfos);
        }
        return success;
    }

    @Override
    public Boolean defaultStatisticsConfig() {
        String userName = SystemHelper.getCurrentUser().getUsername();
        String groupId = userService.getCurrentUserOrg().getUuid();
        Boolean success = true;
        List<StatisticsConfigInfo> statisticsConfigInfos = statisticsDao.getStatistics(groupId, 0);
        statisticsDao.deleteStatisticsConfig(groupId, 1);
        if (!statisticsConfigInfos.isEmpty()) {
            for (StatisticsConfigInfo statisticsConfigInfo : statisticsConfigInfos) {
                statisticsConfigInfo.setId(UUID.randomUUID().toString());
                statisticsConfigInfo.setGroupDefault(1);
                statisticsConfigInfo.setUpdateDataTime(new Date());
                statisticsConfigInfo.setUpdateDataUsername(userName);
            }
            success = statisticsDao.addStatisticsConfig(statisticsConfigInfos);
        }
        return success;
    }

    @Override
    public JSONObject referenceGroup() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        //获取当前组织及其下属组织id
        List<String> groupIds = userService.getCurrentUserOrgIds();
        List<ReferenceGroup> groups = new ArrayList<>();
        for (String orgId : groupIds) {
            if (!groupId.equals(orgId)) {
                //判断该组织是否有综合统计配置信息
                String groupName = statisticsDao.getGroupName(orgId);
                if (groupName != null) {
                    ReferenceGroup referenceGroup = new ReferenceGroup();
                    referenceGroup.setId(orgId);
                    referenceGroup.setName(groupName);
                    groups.add(referenceGroup);
                }
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("grups", JSON.toJSON(groups));
        return jsonObject;
    }
}
