package com.zw.platform.util.privilege;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.common.ZipUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 用户监控对象权限
 */
@Component
public class UserPrivilegeUtil {
    @Autowired
    private UserService userService;

    private static final Logger log = LogManager.getLogger(UserPrivilegeUtil.class);

    public RedisKey getAdasReportCachePrefix(String suffix) {
        return HistoryRedisKeyEnum.ADAS_REPORT_CACHE.of(SystemHelper.getCurrentUserId(), suffix);
    }

    /**
     * 得到当前用户权限范围内的车
     */
    public Set<String> getCurrentUserVehicles() {
        return userService.getCurrentUserMonitorIds();
    }

    /**
     * 判断用户权限是否发生变化
     * 不变 返回 false
     * 变化 返回true
     */
    public boolean judgeUserPrivilege() {

        // 得到当前用户的权限车辆集合
        Set<String> currentVehicles = getCurrentUserVehicles();
        // 从redis 中获取之前的权限车辆集合
        boolean changeMonitorIds = changeMonitorIds(currentVehicles);

        try {
            if (changeMonitorIds) {
                // 删除中寰二期报表的缓存数据(删除redis中前缀为用户uuid_adas_report_)
                RedisHelper.deleteScanKeys(getAdasReportCachePrefix("*"));
                // 把新的权限放入redis中
                if (currentVehicles.size() != 0) {
                    addUserPrivilege2Redis(ZipUtil.gzip(JSON.toJSONString(currentVehicles)));
                }
            }
        } catch (Exception e) {
            log.error("判断用户权限是否发生变化错误", e);
        }
        return changeMonitorIds;
    }

    private boolean changeMonitorIds(Set<String> currentVehicles) {

        String monitorIdList = RedisHelper.getString(getAdasReportCachePrefix(""));
        String str = ZipUtil.gunzip(monitorIdList);
        if (StringUtils.isEmpty(str)) {
            return true;
        }
        List<String> beforeUserVehicle = JSONObject.parseArray(str, String.class);
        // 判断数量
        if (beforeUserVehicle.size() != currentVehicles.size()) {
            return true;
        }
        // 判断内容
        Set<String> oldPriVehicles = Sets.newHashSet();
        oldPriVehicles.addAll(beforeUserVehicle);
        int length = oldPriVehicles.size();
        oldPriVehicles.addAll(currentVehicles);
        if (length != oldPriVehicles.size()) {
            return true;
        }

        return false;

    }

    private void addUserPrivilege2Redis(String newPrivilege) {
        RedisHelper.setString(getAdasReportCachePrefix(""), newPrivilege);
    }

    /**
     * 获取当前用户下的分组id集合
     */
    public Set<String> getCurrentUserAssignSet() {
        String userUid = SystemHelper.getCurrentUserId();
        return getAssignSetByUserName(userUid);
    }

    public Set<String> getAssignSetByUserName(String userName) {
        return userService.getUserGroupIdsByUserName(userName);
    }
}
