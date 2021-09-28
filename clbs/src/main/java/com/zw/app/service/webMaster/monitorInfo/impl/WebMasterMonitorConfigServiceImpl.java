package com.zw.app.service.webMaster.monitorInfo.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.webMaster.alarmType.ReferenceGroup;
import com.zw.app.domain.webMaster.monitorInfo.AppMonitorConfig;
import com.zw.app.domain.webMaster.monitorInfo.AppMonitorConfigInfo;
import com.zw.app.repository.mysql.webMaster.monitorInfo.AppMonitorDao;
import com.zw.app.service.webMaster.monitorInfo.WebMasterMonitorConfigService;
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
 * @date 2019/9/27 16:19
 */
@Service
public class WebMasterMonitorConfigServiceImpl implements WebMasterMonitorConfigService {

    @Autowired
    UserService userService;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    AppMonitorDao appMonitorDao;

    @Override
    public JSONObject getMonitorConfig(String groupId) {
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
        List<AppMonitorConfigInfo> appMonitorConfigInfos = null;
        //通过组织id查询配置信息，id为参考组织id或当前组织id
        appMonitorConfigInfos = appMonitorDao.getMonitorConfigByGroupId(groupId, 0);

        List<AppMonitorConfig> appMonitorConfigs = new ArrayList<>();
        if (!appMonitorConfigInfos.isEmpty()) {
            for (int n = 0; n < appMonitorConfigInfos.size(); n++) {
                AppMonitorConfig appMonitorConfig = new AppMonitorConfig();
                appMonitorConfig.setCategory(appMonitorConfigInfos.get(n).getCategory());
                appMonitorConfig.setType(appMonitorConfigInfos.get(n).getType());
                appMonitorConfig.setName(appMonitorConfigInfos.get(n).getName());
                appMonitorConfigs.add(appMonitorConfig);
            }
        }
        jsonObject.put("monitorConfigs", JSON.toJSON(appMonitorConfigs));
        jsonObject.put("orgId", groupId);
        jsonObject.put("orgName", groupName);
        return jsonObject;
    }

    @Override
    public Boolean updateMonitorConfig(JSONArray jsonArray, String groupId) {
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
        appMonitorDao.deleteGroupMonitorConfig(groupId, 0);
        List<AppMonitorConfigInfo> updateAppMonitorConfigInfos = new ArrayList<>();
        if (jsonArray.size() > 0) {
            Long time = new Date().getTime();
            for (int i = 0; i < jsonArray.size(); i++) {
                AppMonitorConfigInfo appMonitorConfigInfo = new AppMonitorConfigInfo();
                appMonitorConfigInfo.setId(UUID.randomUUID().toString());
                appMonitorConfigInfo.setUpdateDataUsername(userName);
                appMonitorConfigInfo.setUpdateDataTime(new Date(time + 1000 * i));
                appMonitorConfigInfo.setGroupDefault(0);
                appMonitorConfigInfo.setGroupId(groupId);
                appMonitorConfigInfo.setGroupName(groupName);
                appMonitorConfigInfo.setCategory(jsonArray.getJSONObject(i).getString("category"));
                appMonitorConfigInfo.setName(jsonArray.getJSONObject(i).getString("name"));
                appMonitorConfigInfo.setType(jsonArray.getJSONObject(i).getString("type"));
                appMonitorConfigInfo.setAppVersion(jsonArray.getJSONObject(i).getInteger("appVersion"));
                updateAppMonitorConfigInfos.add(appMonitorConfigInfo);
            }
            success = appMonitorDao.addGroupMonitorConfig(updateAppMonitorConfigInfos);
        }
        return success;
    }

    @Override
    public Boolean resetMonitorConfig() {
        String userName = SystemHelper.getCurrentUser().getUsername();
        String groupId = userService.getCurrentUserOrg().getUuid();
        Boolean success = true;
        List<AppMonitorConfigInfo> defaultappMonitorConfigInfos = appMonitorDao.getMonitorConfigByGroupId(groupId, 1);
        appMonitorDao.deleteGroupMonitorConfig(groupId, 0);
        if (!defaultappMonitorConfigInfos.isEmpty()) {
            Long time = new Date().getTime();
            for (int i = 0; i < defaultappMonitorConfigInfos.size(); i++) {
                AppMonitorConfigInfo defaultAppMonitorConfigInfo = defaultappMonitorConfigInfos.get(i);
                defaultAppMonitorConfigInfo.setId(UUID.randomUUID().toString());
                defaultAppMonitorConfigInfo.setGroupDefault(0);
                defaultAppMonitorConfigInfo.setUpdateDataTime(new Date(time + 1000 * i));
                defaultAppMonitorConfigInfo.setUpdateDataUsername(userName);
            }
            success = appMonitorDao.addGroupMonitorConfig(defaultappMonitorConfigInfos);
        }
        return success;
    }

    @Override
    public Boolean defaultMonitorConfig() {
        String userName = SystemHelper.getCurrentUser().getUsername();
        String groupId = userService.getCurrentUserOrg().getUuid();
        Boolean success = true;
        List<AppMonitorConfigInfo> appMonitorConfigInfos = appMonitorDao.getMonitorConfigByGroupId(groupId, 0);
        appMonitorDao.deleteGroupMonitorConfig(groupId, 1);
        if (!appMonitorConfigInfos.isEmpty()) {
            Long time = new Date().getTime();
            for (int i = 0; i < appMonitorConfigInfos.size(); i++) {
                AppMonitorConfigInfo appMonitorConfigInfo = appMonitorConfigInfos.get(i);
                appMonitorConfigInfo.setId(UUID.randomUUID().toString());
                appMonitorConfigInfo.setGroupDefault(1);
                appMonitorConfigInfo.setUpdateDataTime(new Date(time + 1000 * i));
                appMonitorConfigInfo.setUpdateDataUsername(userName);
            }
            success = appMonitorDao.addGroupMonitorConfig(appMonitorConfigInfos);
        }
        return success;
    }

    @Override
    public JSONObject referenceGroup(String type) {
        String groupId = userService.getCurrentUserOrg().getUuid();
        //获取当前组织及其下属组织id
        List<String> groupIds = userService.getCurrentUserOrgIds();
        List<ReferenceGroup> groups = new ArrayList<>();
        for (String orgId : groupIds) {
            if (!groupId.equals(orgId)) {
                //判断该组织是否有以配置信息
                String groupName = appMonitorDao.getGroupName(orgId, type);
                if (groupName != null) {
                    ReferenceGroup referenceGroup = new ReferenceGroup();
                    referenceGroup.setId(orgId);
                    referenceGroup.setName(groupName);
                    groups.add(referenceGroup);
                }
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("groups", JSON.toJSON(groups));
        return jsonObject;
    }
}

