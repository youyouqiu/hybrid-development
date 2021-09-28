package com.zw.platform.service.switching.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.RedisException;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.vas.switching.SwitchingSignal;
import com.zw.platform.event.ConfigUnbindVehicleEvent;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.controller.UserCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.vas.SwitchingSignalDao;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.service.switching.SwitchingSignalService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MonitorTypeUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.RedisSensorQuery;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p> Title:开关信号管理ServiceImpl <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @author nixiangqian
 * @version 1.0
 * @date 2017年06月21日 14:07
 */
@Service
public class SwitchingSignalServiceImpl implements SwitchingSignalService {
    private static Logger log = LogManager.getLogger(SwitchingSignalServiceImpl.class);

    @Autowired
    private SwitchingSignalDao signalDao;

    @Autowired
    private UserService userService;

    @Autowired
    private SendTxtService sendTxtService;

    private RedisVehicleService redisVehicleService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private MonitorTypeUtil monitorTypeUtil;

    @Value("${io.null}")
    private String ioNull;

    @Value("${terminal.off.line}")
    private String terminalOffLine;

    @Value("${vehicle.set.null}")
    private String vehicleSetNull;

    @Autowired
    public void setRedisVehicleService(RedisVehicleService redisVehicleService) {
        this.redisVehicleService = redisVehicleService;
    }

    @Override
    public Page<SwitchingSignal> findByPage(SensorConfigQuery query) throws Exception {
        Page<SwitchingSignal> list = new Page<>();
        RedisSensorQuery redisQuery =
            new RedisSensorQuery(query.getGroupId(), query.getAssignmentId(), query.getSimpleQueryParam(),
                query.getProtocol());

        List<String> vehicleList = redisVehicleService.getVehicleByType(redisQuery, null);
        if (vehicleList == null) {
            throw new RedisException(">=======redis 缓存出错了===========<");
        }
        int listSize = vehicleList.size();
        int curPage = query.getPage().intValue();// 当前页
        int pageSize = query.getLimit().intValue(); // 每页条数
        int lst = (curPage - 1) * pageSize;// 遍历开始条数
        int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);// 遍历条数

        List<String> vehicles = new ArrayList<String>();

        for (int i = 0; i < vehicleList.size(); i++) {
            if (i >= lst && i < ps) {
                vehicles.add(vehicleList.get(i));
            }
        }
        if (CollectionUtils.isEmpty(vehicles)) {
            return list;
        }
        List<SwitchingSignal> switchList = signalDao.findByPageRedis(vehicles);
        if (switchList != null && switchList.size() > 0) {
            Set<String> vids =
                switchList.stream().map(SwitchingSignal::getVehicleId).collect(Collectors.toSet());
            Map<String, VehicleDTO> bindInfos = MonitorUtils.getVehicleMap(vids);
            for (SwitchingSignal switchingSignal : switchList) {
                final VehicleDTO bindInfo = bindInfos.get(switchingSignal.getVehicleId());
                if (null == bindInfo) {
                    continue;
                }
                if (0 == switchingSignal.getMonitorType()) {
                    switchingSignal.setVehicleType(bindInfo.getVehicleTypeName());
                } else if (2 == switchingSignal.getMonitorType()) {
                    switchingSignal.setVehicleType("其他物品");
                }
            }
        }
        VehicleUtil.sort(switchList, vehicles);
        list = RedisQueryUtil.getListToPage(switchList, query, listSize);
        // 处理result，将groupId对应的groupName给result相应的值赋上
        if (list.size() > 0) {
            Set<String> vids =
                list.stream().map(SwitchingSignal::getVehicleId).collect(Collectors.toSet());
            Map<String, BindDTO> bindInfos = VehicleUtil.batchGetBindInfosByRedis(vids);
            for (SwitchingSignal parameter : list) {
                // 将groupId对应的groupName
                BindDTO bindDTO = bindInfos.get(parameter.getVehicleId());
                if (bindDTO != null) {
                    parameter.setGroups(bindDTO.getOrgName());
                }
            }
        }
        return list;
    }

    @Override
    public List<SwitchingSignal> findVehicleSensorSetting(List<Integer> protocols) throws Exception {
        String userId = userService.getCurrentUserUuid();
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getCurrentUserOrgIds();
        return this.signalDao.findVehicleSensorSetting(userId, orgList, protocols);
    }

    @Override
    public SwitchingSignal findByVehicleId(String vehicleId) throws Exception {
        return this.signalDao.findByVehicleId(vehicleId);
    }

    @Override
    public Integer[] findAirStatus(String vehicleId) throws Exception {
        SwitchingSignal signal = signalDao.findByVehicleId(vehicleId);
        Integer[] str = new Integer[2];// str[0] 1、2、3、4 Io信号位与HbaseIO位置对应，str[1] 1常开,2常关，0 无
        str[0] = 0;
        str[1] = 0;
        if (signal != null) {
            if ("01".equals(signal.getZeroId())) { // 第一个IO口对应的常开或是常闭
                str[0] = 1;
                str[1] = signal.getZeroType();
            } else if ("01".equals(signal.getOneId())) { // 第二个IO口对应的常开或是常闭
                str[0] = 2;
                str[1] = signal.getOneType();
            } else if ("01".equals(signal.getTwoId())) { // 第三个IO口对应的常开或是常闭
                str[0] = 3;
                str[1] = signal.getTwoType();
            } else if ("01".equals(signal.getThreeId())) { // 第四个IO口对应的常开或是常闭
                str[0] = 4;
                str[1] = signal.getThreeType();
            }
            return str; // 应该不存在第五接口，但是还是要返回一个值避免错误
        } else {
            return str; // 未绑定该传感器，或，IO接口都不存在空调状态
        }
    }

    @Override
    public SwitchingSignal findById(String id) throws Exception {
        return this.signalDao.findById(id);
    }

    @Override
    public JsonResultBean addSwitchingSignal(SwitchingSignal signal, String ipAddress) throws Exception {
        if (signal.getSignalOne() == null && signal.getSignalThree() == null && signal.getSignalTwo() == null
            && signal.getSignalZero() == null) {
            return new JsonResultBean(JsonResultBean.FAULT, ioNull);
        }
        signal.setCreateDataTime(new Date());
        signal.setCreateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = signalDao.addSwitchingSignal(signal);
        if (flag) {
            final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(signal.getVehicleId());
            if (bindInfo != null) {
                String msg = "监控对象 : " + bindInfo.getName() + " ( @" + bindInfo.getOrgName() + ")  设置信号位";
                final String plateColor = PlateColor.getNameOrBlankByCode(bindInfo.getPlateColor());
                logSearchService.addLog(ipAddress, msg, "3", "开关信号管理", bindInfo.getName(), plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public Boolean updateSwitchingSignal(SwitchingSignal signal, String ipAddress) throws Exception {
        signal.setUpdateDataTime(new Date());
        signal.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = signalDao.updateSwitchingSignal(signal);
        if (flag) {
            final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(signal.getVehicleId());
            if (bindInfo != null) {
                String brand = bindInfo.getName();
                final String plateColor = PlateColor.getNameOrBlankByCode(bindInfo.getPlateColor());
                String groupName = bindInfo.getOrgName();
                String msg = "监控对象 : " + brand + " ( @" + groupName + " ) 修改信号位";
                logSearchService.addLog(ipAddress, msg, "3", "开关信号管理", brand, plateColor);
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean deleteById(String id, String ipAddress) throws Exception {
        SwitchingSignal signal = findById(id);
        if (signal == null) {
            return false;
        }
        String vehicleId = signal.getVehicleId();
        String[] vehicle = logSearchService.findCarMsg(vehicleId);

        boolean flag = signalDao.deleteById(id);

        String msg = "监控对象 : " + signal.getBrand() + " 删除开关信号设置参数";
        logSearchService.addLog(ipAddress, msg, "3", "开关信号管理", vehicle[0], vehicle[1]);
        return flag;

    }

    @Override
    public JsonResultBean deleteBatchByIds(List<String> ids, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        StringBuilder brand = new StringBuilder();
        for (String id : ids) {
            SwitchingSignal signal = signalDao.findById(id);
            if (signal == null) {
                continue;
            }
            brand.append(signal.getBrand()).append(",");
            message.append("监控对象 : ").append(signal.getBrand()).append(" 删除开关信号设置参数 <br/>");
        }
        if ("".equals(brand.toString())) {
            return new JsonResultBean(JsonResultBean.FAULT, vehicleSetNull);
        }
        boolean flag = signalDao.deleteBatchByIds(ids);
        if (flag) {
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除开关信号设置参数");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean sendPosition(String vehicleId, String ipAddress) throws Exception {
        final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindInfo != null) {
            String deviceNumber = bindInfo.getDeviceNumber();
            // 序列号
            Integer msgSn = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
            if (msgSn != null) { // 设备已经注册
                // 订阅回应的user
                String username = SystemHelper.getCurrentUsername();
                UserCache.put(Converter.toBlank(msgSn), username);

                String deviceId = bindInfo.getDeviceId();
                String simcardNumber = bindInfo.getSimCardNumber();
                SubscibeInfo subscibeInfo = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSn,
                    ConstantUtil.T808_GPS_INFO_ACK);
                SubscibeInfoCache.getInstance().putTable(subscibeInfo);
                sendTxtService.deviceLocationQuery(deviceId, simcardNumber, msgSn, bindInfo);
            }
            String msgId = Converter.toBlank(msgSn);
            if (msgId != null && !msgId.equals("0") && !msgId.equals("")) {
                String username = SystemHelper.getCurrentUsername();
                JSONObject json = new JSONObject();
                json.put("msgId", msgId);
                json.put("userName", username);
                String brand = bindInfo.getName();
                String msg = "监控对象 : " + brand + " 下发开关信号位状态";
                String[] vehicleStr = logSearchService.findCarMsg(vehicleId);
                logSearchService.addLog(ipAddress, msg, "2", "开关信号管理", vehicleStr[0], vehicleStr[1]);
                return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
    }

    @Override
    public Boolean deleteConfigByVehicleId(String vehicleId, Integer type) {
        if (StringUtils.isNotEmpty(vehicleId)) {
            if (ConfigUnbindVehicleEvent.TYPE_SINGLE == 0) {
                return signalDao.deleteByVehicleId(vehicleId);
            } else {
                List<String> monitorIds = Arrays.asList(vehicleId.split(","));
                return signalDao.deleteBatchByMonitorIds(monitorIds);
            }
        }
        return false;
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent event) {
        List<String> monitorIds = event.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toList());
        signalDao.deleteBatchByMonitorIds(monitorIds);
    }
}
