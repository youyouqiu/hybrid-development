package com.zw.app.service.webMaster.alarmType.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.webMaster.alarmType.AlarmType;
import com.zw.app.domain.webMaster.alarmType.AppAlarmConfigInfo;
import com.zw.app.domain.webMaster.alarmType.ReferenceGroup;
import com.zw.app.repository.mysql.webMaster.alarmType.AppAlarmTypeDao;
import com.zw.app.service.webMaster.alarmType.WebMasterAlarmTypeService;
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
 * @date 2018/8/28 09:19
 */
@Service
public class WebMasterAlarmTypeServiceImpl implements WebMasterAlarmTypeService {
    @Autowired
    AppAlarmTypeDao appAlarmTypeDao;
    @Autowired
    UserService userService;
    @Autowired
    OrganizationService organizationService;


    /**
     * 获取报警参数配置
     *
     * @author lijie
     * @date 2018/8/28 09:30
     */
    @Override
    public JSONObject getAlarmType(String groupId) {
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
        List<AppAlarmConfigInfo> appAlarmConfigInfos = null;
        //通过组织id查询配置信息，id为参考组织id或当前组织id
        appAlarmConfigInfos = appAlarmTypeDao.getAlarmType(groupId, 0);

        List<AlarmType> alarmTypes = new ArrayList<>();
        if (!appAlarmConfigInfos.isEmpty()) {
            for (int n = 0; n < appAlarmConfigInfos.size(); n++) {
                AlarmType alarmType = new AlarmType();
                alarmType.setCategory(appAlarmConfigInfos.get(n).getCategory());
                alarmType.setType(appAlarmConfigInfos.get(n).getType());
                alarmType.setName(appAlarmConfigInfos.get(n).getName());
                alarmTypes.add(alarmType);
            }
        }
        jsonObject.put("alarmTypes", JSON.toJSON(alarmTypes));
        jsonObject.put("orgId", groupId);
        jsonObject.put("orgName", groupName);
        return jsonObject;
    }

    /**
     * 修改报警参数配置
     *
     * @author lijie
     * @date 2018/8/28 16:10
     */
    @Override
    public Boolean updateAlarmType(JSONArray jsonArray, String groupId) {
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
        List<AppAlarmConfigInfo> appAlarmConfigInfos = appAlarmTypeDao.getAlarmType(groupId, 0);
        if (!appAlarmConfigInfos.isEmpty()) {
            appAlarmTypeDao.deleteGroupAlarmType(groupId, 0);
        }
        List<AppAlarmConfigInfo> updateAppAlarmConfigInfos = new ArrayList<>();
        if (jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                AppAlarmConfigInfo appAlarmConfigInfo = new AppAlarmConfigInfo();
                appAlarmConfigInfo.setId(UUID.randomUUID().toString());
                appAlarmConfigInfo.setUpdateDataUsername(userName);
                appAlarmConfigInfo.setUpdateDataTime(new Date(new Date().getTime() + 1000 * i));
                appAlarmConfigInfo.setGroupDefault(0);
                appAlarmConfigInfo.setGroupId(groupId);
                appAlarmConfigInfo.setGroupName(groupName);
                appAlarmConfigInfo.setCategory(jsonArray.getJSONObject(i).getString("category"));
                appAlarmConfigInfo.setName(jsonArray.getJSONObject(i).getString("name"));
                appAlarmConfigInfo.setType(jsonArray.getJSONObject(i).getString("type"));
                appAlarmConfigInfo.setAppVersion(jsonArray.getJSONObject(i).getInteger("appVersion"));
                updateAppAlarmConfigInfos.add(appAlarmConfigInfo);
            }
            success = appAlarmTypeDao.addGroupAlarmType(updateAppAlarmConfigInfos);
        }
        return success;
    }

    /**
     * 恢复报警参数配置为组织默认
     *
     * @author lijie
     * @date 2018/8/28 17:32
     */
    @Override
    public Boolean resetAlarmType() {
        String userName = SystemHelper.getCurrentUser().getUsername();
        String groupId = userService.getCurrentUserOrg().getUuid();
        Boolean success = true;
        List<AppAlarmConfigInfo> defaultAppAlarmConfigInfos = appAlarmTypeDao.getAlarmType(groupId, 1);
        appAlarmTypeDao.deleteGroupAlarmType(groupId, 0);
        if (!defaultAppAlarmConfigInfos.isEmpty()) {
            for (AppAlarmConfigInfo defaultAppAlarmConfigInfo : defaultAppAlarmConfigInfos) {
                defaultAppAlarmConfigInfo.setId(UUID.randomUUID().toString());
                defaultAppAlarmConfigInfo.setGroupDefault(0);
                defaultAppAlarmConfigInfo.setUpdateDataTime(new Date());
                defaultAppAlarmConfigInfo.setUpdateDataUsername(userName);
            }
            success = appAlarmTypeDao.addGroupAlarmType(defaultAppAlarmConfigInfos);
        }

        return success;
    }

    /**
     * 设置报警参数配置为组织默认
     *
     * @author lijie
     * @date 2018/8/28 18:10
     */
    @Override
    public Boolean defaultAlarmType() {
        String userName = SystemHelper.getCurrentUser().getUsername();
        String groupId = userService.getCurrentUserOrg().getUuid();
        Boolean success = true;
        List<AppAlarmConfigInfo> appAlarmConfigInfos = appAlarmTypeDao.getAlarmType(groupId, 0);
        appAlarmTypeDao.deleteGroupAlarmType(groupId, 1);
        if (!appAlarmConfigInfos.isEmpty()) {
            for (AppAlarmConfigInfo appAlarmConfigInfo : appAlarmConfigInfos) {
                appAlarmConfigInfo.setId(UUID.randomUUID().toString());
                appAlarmConfigInfo.setGroupDefault(1);
                appAlarmConfigInfo.setUpdateDataTime(new Date());
                appAlarmConfigInfo.setUpdateDataUsername(userName);
            }
            success = appAlarmTypeDao.addGroupAlarmType(appAlarmConfigInfos);
        }
        return success;
    }

    /**
     * 获取参考组织信息
     *
     * @author lijie
     * @date 2018/8/28 18:20
     */
    @Override
    public JSONObject referenceGroup() {
        String groupId = userService.getCurrentUserOrg().getUuid();
        //获取当前组织及其下属组织id
        List<String> groupIds = userService.getCurrentUserOrgIds();
        List<ReferenceGroup> groups = new ArrayList<>();
        for (String orgId : groupIds) {
            if (!groupId.equals(orgId)) {
                //判断该组织是否有以配置信息
                String groupName = appAlarmTypeDao.getGroupName(orgId);
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

