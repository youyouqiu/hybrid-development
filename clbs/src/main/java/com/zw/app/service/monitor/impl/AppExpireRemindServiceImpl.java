package com.zw.app.service.monitor.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.ISelect;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.domain.expireDate.AppExpireDateEntity;
import com.zw.app.domain.expireDate.AppExpireDateQuery;
import com.zw.app.entity.monitor.AppExpireRemindDetailQueryEntity;
import com.zw.app.repository.mysql.expireDate.AppExpireDateMysqlDao;
import com.zw.app.service.monitor.AppExpireRemindService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.expireRemind.ExpireRemindInstant;
import com.zw.platform.util.CosUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StrUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/***
 * app到期提醒
 * @author zhengjc
 * @since  2019/11/21 17:26
 * @version 1.0
 **/
@AppServerVersion
@Service
public class AppExpireRemindServiceImpl implements AppExpireRemindService {

    @Autowired
    UserService userService;

    @Autowired
    private AppExpireDateMysqlDao expireDateDao;

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/expireRemind/getExpireRemindInfos" })
    public Map<String, Object> getExpireRemindInfos(String userName) {
        Map<String, RedisKey> keyExpireModule = getAppExpireKeys();
        return countVehicleExpireRemind(keyExpireModule);
    }

    private Map<String, RedisKey> getWebExpireKeys() {
        Map<String, RedisKey> keyExpireModule = new HashMap<>();
        keyExpireModule.put("expireDrivingLicenseList", HistoryRedisKeyEnum.EXPIRE_DRIVING_LICENSE.of());
        keyExpireModule.put("expireRoadTransportList", HistoryRedisKeyEnum.EXPIRE_ROAD_TRANSPORT.of());
        keyExpireModule.put("expireMaintenanceList", HistoryRedisKeyEnum.EXPIRE_MAINTENANCE.of());
        return keyExpireModule;
    }

    private Map<String, RedisKey> getAppExpireKeys() {
        Map<String, RedisKey> keyExpireModule = getWebExpireKeys();
        keyExpireModule.put("alreadyExpireRoadTransportList", HistoryRedisKeyEnum.ALREADY_EXPIRE_ROAD_TRANSPORT.of());
        keyExpireModule.put("alreadyExpireDrivingLicenseList", HistoryRedisKeyEnum.ALREADY_EXPIRE_DRIVING_LICENSE.of());
        keyExpireModule.put("expireInsuranceIdList", HistoryRedisKeyEnum.EXPIRE_INSURANCE.of());
        keyExpireModule.put("lifecycleExpireNumber", HistoryRedisKeyEnum.EXPIRE_LIFE_CYCLE_REMIND.of());
        keyExpireModule.put("alreadyLifecycleExpireNumber", HistoryRedisKeyEnum.ALREADY_EXPIRE_LIFE_CYCLE.of());
        return keyExpireModule;
    }

    /**
     * 统计车辆到期的提醒
     * 行驶证到期车辆数、 运输证到期车辆数、 车辆保养到期车辆数
     */
    private Map<String, Object> countVehicleExpireRemind(Map<String, RedisKey> keyExpireModule) {
        Map<String, Object> resultMap = new HashMap<>();
        Set<String> userOwnVehicleIds = getGroupVehicle();
        if (CollectionUtils.isNotEmpty(userOwnVehicleIds)) {
            for (Map.Entry<String, RedisKey> entry : keyExpireModule.entrySet()) {
                resultMap.put(entry.getKey(), getExpireVehIds(entry.getValue(), userOwnVehicleIds).size());
            }
        }
        return resultMap;
    }

    private List<String> getExpireVehIds(RedisKey module, Set<String> userOwnVehicleIdList) {
        List<String> moduleVehIds = new ArrayList<>();
        String val = RedisHelper.getString(module);
        if (StrUtil.isBlank(val)) {
            return moduleVehIds;
        }
        moduleVehIds = JSON.parseArray(val, String.class);
        return filterVehicleByOwnVehicleIds(userOwnVehicleIdList, moduleVehIds);
    }

    /**
     * 根据用户拥有的车辆过滤
     */
    private List<String> filterVehicleByOwnVehicleIds(Set<String> userOwnVehicleIdList,
        List<String> expireVehicleIdList) {
        List<String> expireVehIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(expireVehicleIdList)) {
            expireVehIds =
                expireVehicleIdList.stream().filter(userOwnVehicleIdList::contains).collect(Collectors.toList());
        }
        return expireVehIds;
    }

    private Set<String> getGroupVehicle() {
        List<String> sortVehicleIds = RedisHelper.getList(RedisKeyEnum.VEHICLE_SORT_LIST.of());
        // 获取分组权限内绑定的车id
        Set<String> ownVehicleIds = userService.getCurrentUserMonitorIds();
        // 获取组织及下级组织中游离的车id
        ownVehicleIds.addAll(userService.getCurrentUserUnbindMonitorIds(userService.getCurrentUserOrgIds()));
        // 用户权限的所有车id
        // 筛选权限数据，并排序
        Set<String> sortAssignVehicle = new LinkedHashSet<>(ownVehicleIds.size());
        if (CollectionUtils.isNotEmpty(sortVehicleIds)) {
            for (String vid : sortVehicleIds) {
                if (ownVehicleIds.contains(vid)) {
                    sortAssignVehicle.add(vid);
                }
            }
        }
        return sortAssignVehicle;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/expireRemind/getExpireRemindInfoDetails" })
    public List<AppExpireDateEntity> getExpireRemindInfoDetails(AppExpireRemindDetailQueryEntity erqe) {
        int typeVal = erqe.getType();
        List<AppExpireDateEntity> result = new ArrayList<>();
        RedisKey moduleKey = ExpireRemindInstant.getExpireKey(typeVal);
        String redisKeyStr = moduleKey.get();
        Set<String> userOwnVehicleIds = getGroupVehicle();
        List<String> vehicleIds = getExpireVehIds(moduleKey, userOwnVehicleIds);
        if (CosUtil.areNotEmpty(vehicleIds, userOwnVehicleIds)) {
            ISelect select = getSelect(redisKeyStr, vehicleIds);
            result = PageHelperUtil.doSelect(AppExpireDateQuery.getInstance(erqe), select);
        }
        // 如果查询的是车保险到期,会有多条数据 需要根据具体的保险单id 筛选数据
        if (CollectionUtils.isNotEmpty(result) && Objects.equals(ExpireRemindInstant.expireInsurance, redisKeyStr)) {
            RedisKey redisKey = HistoryRedisKeyEnum.EXPIRE_INSURANCE_ID.of();
            Set<String> vehicleInsuranceIds = new HashSet<>(RedisHelper.getList(redisKey));
            result = result.stream().filter(obj -> {
                String id = obj.getId();
                return StringUtils.isNotBlank(id) && vehicleInsuranceIds.contains(id);
            }).collect(Collectors.toList());
        }
        return result;
    }

    private ISelect getSelect(String moduleKey, List<String> vehicleIds) {
        ISelect select = null;
        Set<String> vehIdSet = new HashSet<>(vehicleIds);
        if (ExpireRemindInstant.expireLifeCycle.equals(moduleKey) || ExpireRemindInstant.alreadyExpireLifeCycle
            .equals(moduleKey)) {
            select = () -> expireDateDao.findLifecycleExpireList(vehIdSet);
        } else if (ExpireRemindInstant.expireDrivingLicense.equals(moduleKey)
            || ExpireRemindInstant.alreadyExpireDrivingLicense.equals(moduleKey)) {
            select = () -> expireDateDao.findDrivingLicenseExpireList(vehIdSet);
        } else if (ExpireRemindInstant.expireRoadTransport.equals(moduleKey)
            || ExpireRemindInstant.alreadyExpireRoadTransport.equals(moduleKey)) {
            select = () -> expireDateDao.findRoadTransportExpireList(vehIdSet);
        } else if (ExpireRemindInstant.expireMaintenance.equals(moduleKey)) {
            select = () -> expireDateDao.findMaintenanceExpireList(vehIdSet);
        } else if (ExpireRemindInstant.expireInsurance.equals(moduleKey)) {
            select = () -> expireDateDao.findInsuranceExpireList(vehIdSet);
        }
        return select;
    }

}
