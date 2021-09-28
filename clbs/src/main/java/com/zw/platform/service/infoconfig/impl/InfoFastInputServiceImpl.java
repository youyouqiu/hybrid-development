package com.zw.platform.service.infoconfig.impl;

import com.zw.platform.basic.domain.LifecycleDO;
import com.zw.platform.basic.repository.NewLifecycleDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.SimcardInfo;
import com.zw.platform.domain.basicinfo.ThingInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.repository.modules.InfoFastInputDao;
import com.zw.platform.service.basicinfo.SimcardService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.infoconfig.InfoFastInputService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.topspeed.TopSpeedService;
import com.zw.platform.util.common.Converter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
@Slf4j
@Deprecated
public class InfoFastInputServiceImpl implements InfoFastInputService {

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private InfoFastInputDao infoFastInputDao;

    @Autowired
    private NewLifecycleDao newLifecycleDao;

    @Autowired
    private UserService userService;

    @Autowired
    private SimcardService simcardService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private TopSpeedService topSpeedService;

    @Override
    public List<VehicleInfo> getVehicleInfoList() throws Exception {
        // 根据userName获取userId
        String userId = SystemHelper.getCurrentUser().getId().toString();
        List<String> userOrgListId = userService.getOrgUuidsByUser(userId);
        String uuid = userService.getUserUuidById(userId);
        return infoFastInputDao.getVehicleInfoList(uuid, userOrgListId);
    }

    @Override
    public List<VehicleInfo> getPeopleInfoList() throws Exception {
        // 根据userName获取userId
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所在组织及下级组织
        List<String> userOrgListId = userService.getOrgUuidsByUser(userId);
        List<VehicleInfo> result = new ArrayList<>();
        if (userId != null) {
            result = infoFastInputDao.getPeopleInfoList(userService.getUserUuidById(userId), userOrgListId);
        }
        return result;
    }

    @Override
    public List<ThingInfo> getThingInfoList() throws Exception {
        return infoFastInputDao.getThingInfoList();
    }

    @Override
    public List<DeviceInfo> getdeviceInfoList() throws Exception {
        List<String> list = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        return infoFastInputDao.getdeviceInfoList(list);
    }

    @Override
    public List<SimcardInfo> getSimcardInfoList() throws Exception {
        List<String> list = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        return infoFastInputDao.getSimcardInfoList(list);
    }

    @Override
    public List<Group> getgetGroupList() throws Exception {
        return infoFastInputDao.getGroupList();
    }

    /**
     * 添加生命周期以及信息配置绑定关系
     * @param form
     * @throws Exception
     */

    public void add(Config1Form form) throws Exception {
        LifecycleDO lifecycleDO = new LifecycleDO();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        lifecycleDO.setBillingDate(calendar.getTime());
        calendar.add(Calendar.YEAR, +1);
        calendar.add(Calendar.SECOND, -1);
        lifecycleDO.setExpireDate(calendar.getTime());
        lifecycleDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        newLifecycleDao.insert(lifecycleDO);
        form.setServiceLifecycleId(lifecycleDO.getId());
        form.setBillingDate(lifecycleDO.getBillingDate());
        form.setDueDate(lifecycleDO.getExpireDate());
        infoFastInputDao.addConfigList(form);
    }

    @Override
    public List<DeviceInfo> getDeviceInfoListForPeople() throws Exception {
        List<String> list = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        return infoFastInputDao.getDeviceInfoListForPeople(list);
    }


    /**
     * 添加日志
     * @param form
     * @param ip
     * @param type(1:快速录入，2:极速录入，3:扫码录入)
     * @throws Exception
     * @author fanlu
     */

    public void addLog(Config1Form form, String ip, int type) throws Exception {
        String msg =
            "：添加 " + form.getBrands() + "(监控对象)," + form.getDevices() + "(终端号)," + form.getSims() + "(终端手机号)的绑定关系";
        String opType = "：添加绑定关系";
        switch (type) {
            case 1:
                msg = "快速录入" + msg;
                opType = "快速录入" + opType;
                break;
            case 2:
                msg = "极速录入" + msg;
                opType = "极速录入" + opType;
                // 过滤框内时间
                String uniqueNumber = form.getUniqueNumber();
                if (uniqueNumber.contains("（")) {
                    uniqueNumber = uniqueNumber.substring(0, uniqueNumber.indexOf("（"));
                }
                topSpeedService.deleteByDeviceId(uniqueNumber);
                break;
            case 3:
                msg = "扫码录入" + msg;
                opType = "扫码录入" + opType;
                topSpeedService.deleteByDeviceId(form.getUniqueNumber());
                break;
            default:
                break;
        }
        logSearchService.addLog(ip, msg, "3", "more", opType);
    }

    /**
     * break;
     * }
     * logSearchService.addLog(ip, msg, "3", "more", opType);
     * }
     * /**
     * 初始化config详情，用于保存至redis
     * @param config1Form
     * @param assignmentName
     * @return
     * @author fanlu
     */

    public ConfigList initConfigList(Config1Form config1Form, String assignmentName) {
        // 维护信息配置缓存, config列表：车Key--config详细
        String vehicleId = config1Form.getBrandID();
        ConfigList cl = new ConfigList();
        cl.setConfigId(config1Form.getId());
        cl.setCarLicense(config1Form.getBrands());
        cl.setVehicleId(vehicleId);
        cl.setGroupId(config1Form.getGroupid());
        cl.setGroupName(config1Form.getGroupName());
        cl.setAssignmentName(assignmentName);
        cl.setAssignmentId(config1Form.getCitySelID().replaceAll(";", ","));
        cl.setDeviceNumber(config1Form.getDevices());
        cl.setDeviceType(config1Form.getDeviceType());
        cl.setFunctionalType(config1Form.getFunctionalType());
        cl.setSimcardNumber(config1Form.getSims());
        cl.setCreateDateTime(DateFormatUtils.format(new Date(), DATE_FORMAT));
        cl.setVehicleType(config1Form.getVehicleType());
        cl.setMonitorType(config1Form.getMonitorType());
        cl.setDeviceId(config1Form.getDeviceID());
        cl.setSimcardId(config1Form.getSimID());
        cl.setBillingDate(DateFormatUtils.format(config1Form.getBillingDate(), DATE_FORMAT));
        cl.setExpireDate(DateFormatUtils.format(config1Form.getDueDate(), DATE_FORMAT));
        cl.setVehicleType(config1Form.getVehicleType());
        cl.setIsVideo(config1Form.getIsVideo());
        cl.setPlateColor(config1Form.getPlateColor());
        cl.setType(config1Form.getThingType());
        cl.setCategory(config1Form.getThingCategory());
        cl.setTypeName(config1Form.getThingTypeName());
        cl.setCategoryName(config1Form.getThingCategoryName());
        //设置运营类别
        cl.setVehiclePurpose(config1Form.getVehiclePurpose());
        //设置终端厂商
        cl.setTerminalManufacturer(config1Form.getTerminalManufacturer());
        //设置终端型号
        cl.setTerminalType(config1Form.getTerminalType());
        // 根据sim卡号获取sim卡信息
        SimcardInfo si = simcardService.findBySIMCard(Converter.toBlank(config1Form.getSims()));
        if (Objects.nonNull(si)) {
            cl.setRealId(si.getRealId());
        }
        return cl;
    }

    @Override
    public String getRandomNumbers(String sim, int monitorType) throws Exception {
        StringBuffer brand = new StringBuffer("");
        if (sim.length() > 6 && sim.length() < 21) {
            sim = sim.substring(sim.length() - 5);
            // 设置生成随机数区间
            int min1 = 65;
            int max1 = 91;
            // A-Z字母随机数
            char a = (char) (new Random().nextInt((max1 - min1)) + min1);
            // 拼成监控对象编号
            brand.append("扫").append(a).append(sim);
            List<String> brands;
            switch (monitorType) {
                case 0: //车
                    brands = infoFastInputDao.findScanVehicleByBrand(sim);
                    break;
                case 1: //人
                    brands = infoFastInputDao.findScanPeopleByBrand(sim);
                    break;
                case 2: //物
                    brands = infoFastInputDao.findScanThingByBrand(sim);
                    break;
                default:
                    brands = null;
                    break;
            }
            if (brands == null) {
                return null;
            }
            // 判断该卡号是否已随机全部字母
            if (brands.size() >= 26) {
                brand.append(26);
                return brand.toString();
            }
            // 判断是否重复
            while (brands.contains(brand.toString())) {
                brand = new StringBuffer("");
                a = (char) (new Random().nextInt((max1 - min1)) + min1);
                brand.append("扫").append(a).append(sim);
            }
        }
        return brand.toString();
    }

}
