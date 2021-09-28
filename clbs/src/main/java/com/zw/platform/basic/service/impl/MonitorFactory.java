package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.constant.InputTypeEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.service.MonitorBaseService;
import com.zw.platform.basic.service.PeopleService;
import com.zw.platform.basic.service.ThingService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.MonitorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 监控对象管理工厂
 * @author zhangjuan
 */
@Component
public class MonitorFactory {
    @Autowired
    @Qualifier("vehicleService")
    private VehicleService vehicleService;

    @Autowired
    @Qualifier("peopleService")
    private PeopleService peopleService;

    @Autowired
    @Qualifier("thingService")
    private ThingService thingService;

    public VehicleService getVehicleService() {
        return this.vehicleService;
    }

    public PeopleService getPeopleService() {
        return this.peopleService;
    }

    public ThingService getThingService() {
        return this.thingService;
    }

    public MonitorBaseService create(String monitorType) {
        MonitorBaseService monitorService;
        switch (monitorType) {
            case "0":
                monitorService = this.vehicleService;
                break;
            case "1":
                monitorService = this.peopleService;
                break;
            case "2":
                monitorService = this.thingService;
                break;
            default:
                monitorService = this.vehicleService;
        }
        return monitorService;
    }

    public RedisKey getOrgUnbindKey(String monitorType, String orgId) {
        RedisKey orgUnBindKey;
        switch (monitorType) {
            case "0":
                orgUnBindKey = RedisKeyEnum.ORG_UNBIND_VEHICLE.of(orgId);
                break;
            case "1":
                orgUnBindKey = RedisKeyEnum.ORG_UNBIND_PEOPLE.of(orgId);
                break;
            case "2":
                orgUnBindKey = RedisKeyEnum.ORG_UNBIND_THING.of(orgId);
                break;
            default:
                orgUnBindKey = null;
        }
        return orgUnBindKey;
    }

    /**
     * 获取未绑定监控对象下拉框
     * @param monitorType 监控对象类型
     * @return 监控对象下拉框
     */
    public List<Map<String, Object>> getUbBindSelectList(String monitorType) {
        List<Map<String, Object>> unBindList;
        switch (monitorType) {
            case "0":
                unBindList = vehicleService.getUbBindSelectList();
                break;
            case "1":
                unBindList = peopleService.getUbBindSelectList();
                break;
            case "2":
                unBindList = thingService.getUbBindSelectList();
                break;
            default:
                unBindList = new ArrayList<>();
                break;
        }
        return unBindList;
    }

    /**
     * 获取未绑定监控对象下拉框
     * @param monitorType 监控对象类型
     * @param keyword     监控对象模糊搜索字段
     * @return 监控对象下拉框
     */
    public List<Map<String, Object>> getUbBindSelectList(String monitorType, String keyword) {
        List<Map<String, Object>> unBindList;
        switch (monitorType) {
            case "0":
                unBindList = vehicleService.getUbBindSelectList(keyword);
                break;
            case "1":
                unBindList = peopleService.getUbBindSelectList(keyword);
                break;
            case "2":
                unBindList = thingService.getUbBindSelectList(keyword);
                break;
            default:
                unBindList = new ArrayList<>();
                break;
        }
        return unBindList;
    }

    /**
     * 添加监控对象
     * @param configDTO 信息配置绑定信息
     * @param userOrgIds 用户拥有的组织ID集合
     */
    public void addMonitor(ConfigDTO configDTO, Collection<String> userOrgIds) throws BusinessException {
        String moType = configDTO.getMonitorType();
        MonitorBaseService monitorService = create(moType);
        BindDTO monitorDTO = monitorService.getByName(configDTO.getName());
        boolean isNewAdd = Objects.isNull(monitorDTO);
        if (isNewAdd) {
            monitorDTO = monitorService.getDefaultInfo(configDTO);
            monitorDTO.setBindType(Vehicle.BindType.UNBIND);
            monitorService.add(monitorDTO);
        }
        if (Objects.equals(Vehicle.BindType.HAS_BIND, monitorDTO.getBindType())
                || !userOrgIds.contains(monitorDTO.getOrgId())) {
            throw new BusinessException("不好意思，你来晚了！监控对象【" + configDTO.getName() + "】已被使用");
        }
        String monitorId = monitorDTO.getId();
        configDTO.setId(monitorId);
        configDTO.setBindDate(DateUtil.formatDate(new Date(), DateFormatKey.YYYY_MM_DD_HH_MM_SS));
        configDTO.setBindType(Vehicle.BindType.HAS_BIND);
        configDTO.setOrgId(monitorDTO.getOrgId());
        configDTO.setOrgName(monitorDTO.getOrgName());

        //极速录入时，更新车辆信息里行政区划相关信息
        boolean isTopSpeed = Objects.equals(InputTypeEnum.TOP_SPEED_INPUT, configDTO.getInputType());
        if (!isNewAdd && Objects.equals(MonitorTypeEnum.VEHICLE.getType(), moType) && isTopSpeed) {
            vehicleService.updateDivision(monitorId, configDTO.getProvinceId(), configDTO.getCityId());
        }
    }

    /**
     * 修改监控对象的名称
     * @param bindDTO 监控对象绑定新
     */
    public void updateName(ConfigDTO bindDTO, BindDTO oldBind) throws BusinessException {
        String monitorType = bindDTO.getMonitorType();
        MonitorBaseService monitorService = create(monitorType);
        bindDTO.setUpdateBindDate(DateUtil.formatDate(new Date(), DateFormatKey.YYYY_MM_DD_HH_MM_SS));
        bindDTO.setBindType(Vehicle.BindType.HAS_BIND);
        //判断监控对象是否修改了监控对象名称
        boolean isUpdate = StringUtils.isNotBlank(bindDTO.getName()) && !bindDTO.getName().equals(oldBind.getName());
        if (Objects.equals(bindDTO.getId(), oldBind.getId()) && isUpdate) {
            //监控对象修改为不存在的监控对象，进行名称修改
            monitorService.update(bindDTO.getId(), bindDTO.getName());
            return;
        }

        //监控对象未进行修改
        if (Objects.equals(bindDTO.getId(), oldBind.getId())) {
            bindDTO.setName(oldBind.getName());
            bindDTO.setOrgId(oldBind.getOrgId());
            return;
        }

        //监控对象修改为其他已经存在的监控对象
        BindDTO monitor = MonitorUtils.getBindDTO(bindDTO.getId());
        if (Objects.isNull(monitor)) {
            throw new BusinessException("该监控对象已删除，请确认");
        }
        bindDTO.setName(monitor.getName());
        bindDTO.setOrgId(monitor.getOrgId());
        bindDTO.setOrgName(monitor.getOrgName());

        //删除当前绑定监控对象相关的未绑定缓存
        String fuzzyField = FuzzySearchUtil.buildMonitorField(monitorType, bindDTO.getName());
        RedisHelper.hdel(RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of(), fuzzyField);
        RedisHelper.hdel(getOrgUnbindKey(monitorType, bindDTO.getOrgId()), bindDTO.getId());
        //维护原来的监控对象进行解绑
        unBindMonitor(oldBind);
    }

    /**
     * 监控对象解绑缓存维护
     * @param bindDTO bindDTO
     */
    private void unBindMonitor(BindDTO bindDTO) {
        //维护模未绑定监控对象的模糊搜索缓存
        String monitorType = bindDTO.getMonitorType();
        Map<String, String> fuzzyMap = FuzzySearchUtil.buildMonitor(monitorType, bindDTO.getId(), bindDTO.getName());
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of(), fuzzyMap);

        //维护企业下未绑定的监控对象缓存
        String orgUnBindValue = getUnbindValue(bindDTO);
        String monitorId = bindDTO.getId();
        RedisHelper.addToHash(getOrgUnbindKey(monitorType, bindDTO.getOrgId()), monitorId, orgUnBindValue);
        //维护监控对象缓存绑定
        List<String> bindFields = getBindField();
        RedisHelper.hdel(RedisKeyEnum.MONITOR_INFO.of(monitorId), bindFields);
        RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(monitorId), "bindType", Vehicle.BindType.UNBIND);
    }

    public List<String> getBindField() {
        List<String> fieldSet = new ArrayList<>();
        BindDTO bindDTO = new BindDTO();
        //不需要获取父类的字段
        Field[] fields = bindDTO.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Objects.equals(field.getName(), "plateColor")) {
                continue;
            }
            fieldSet.add(field.getName());
        }
        return fieldSet;
    }

    public String getUnbindValue(BindDTO bindDTO) {
        String orgUnBindValue = bindDTO.getName();
        if (Objects.equals(bindDTO.getMonitorType(), MonitorTypeEnum.VEHICLE.getType())) {
            Map<String, Object> vehicleMap =
                ImmutableMap.of("brand", bindDTO.getName(), "plateColor", bindDTO.getPlateColor());
            orgUnBindValue = JSON.toJSONString(vehicleMap);
        }
        return orgUnBindValue;
    }

}
