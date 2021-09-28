/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with ZhongWei.
 */

package com.zw.platform.service.monitoring.impl;

import com.github.pagehelper.PageHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.param.CameraParam;
import com.zw.platform.domain.param.CommunicationParam;
import com.zw.platform.domain.param.DeviceConnectServerParam;
import com.zw.platform.domain.param.DeviceParam;
import com.zw.platform.domain.param.EventSetParam;
import com.zw.platform.domain.param.GNSSParam;
import com.zw.platform.domain.param.InformationParam;
import com.zw.platform.domain.param.PhoneBookParam;
import com.zw.platform.domain.param.PhoneParam;
import com.zw.platform.domain.param.PositionParam;
import com.zw.platform.domain.param.SerialPortParam;
import com.zw.platform.domain.param.StationParam;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.vas.monitoring.MonitorCommandBindForm;
import com.zw.platform.domain.vas.monitoring.query.RealTimeCommandQuery;
import com.zw.platform.repository.vas.RealTimeCommandDao;
import com.zw.platform.service.monitoring.RealTimeCommandService;
import com.zw.platform.util.common.MethodLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 实时指令
 */
@Service
public class RealTimeCommandServiceImpl implements RealTimeCommandService {
    @Autowired
    RealTimeCommandDao realTimeCommandDao;

    @Override
    public boolean addCommunicationParam(CommunicationParam commParam) {
        commParam.setCreateDataTime(new Date());
        commParam.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return realTimeCommandDao.addCommunicationParam(commParam);
    }

    @Override
    public boolean addCameraParam(CameraParam cameraParam) {
        cameraParam.setCreateDataTime(new Date());
        cameraParam.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return realTimeCommandDao.addCameraParam(cameraParam);
    }

    @Override
    public boolean addDeviceParam(DeviceParam deviceParam) {
        deviceParam.setCreateDataTime(new Date());
        deviceParam.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return realTimeCommandDao.addDeviceParam(deviceParam);
    }

    @Override
    public boolean addGNSSParam(GNSSParam gnssParam) {
        gnssParam.setCreateDataTime(new Date());
        gnssParam.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return realTimeCommandDao.addGNSSParam(gnssParam);
    }

    @Override
    public boolean addPhoneParam(PhoneParam phoneParam) {
        phoneParam.setCreateDataTime(new Date());
        phoneParam.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return realTimeCommandDao.addPhoneParam(phoneParam);
    }

    @Override
    public boolean addPositionParam(PositionParam positionParam) {
        positionParam.setCreateDataTime(new Date());
        positionParam.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return realTimeCommandDao.addPositionParam(positionParam);
    }

    @Override
    public boolean addCommandBind(List<MonitorCommandBindForm> commandBind) {
        return realTimeCommandDao.addCommandBind(commandBind);
    }

    @MethodLog(name = "查询实时指令", description = "查询实时指令")
    @Override
    public List<Map<String, Object>> findRealTimeCommand(
            RealTimeCommandQuery query, List<String> vehicleList, boolean doPage) {
        return doPage
                ? PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> realTimeCommandDao.findRealTimeCommand(query, vehicleList))
                : realTimeCommandDao.findRealTimeCommand(query, vehicleList);
    }

    @Override
    public boolean deleteParamSetting(String vehicleId, int commandType) {
        return realTimeCommandDao.deleteParamSetting(vehicleId, commandType);
    }

    @Override
    public CommunicationParam findCommunicationParam(String id) {
        return realTimeCommandDao.findCommunicationParam(id);
    }

    @Override
    public DeviceParam findDeviceParam(String id) {
        return realTimeCommandDao.findDeviceParam(id);
    }

    @Override
    public GNSSParam findGNSSParam(String id) {
        return realTimeCommandDao.findGNSSParam(id);
    }

    @Override
    public PhoneParam findPhoneParam(String id) {
        return realTimeCommandDao.findPhoneParam(id);
    }

    @Override
    public PositionParam findPositionParam(String id) {

        return realTimeCommandDao.findPositionParam(id);
    }

    @Override
    public CameraParam findCameraParam(String id) {
        return realTimeCommandDao.findCameraParam(id);
    }

    @Override
    public boolean deleteCommandBind(MonitorCommandBindForm commandBind) {
        return realTimeCommandDao.deleteCommandBind(commandBind);
    }

    @Override
    public boolean deleteCommandBindByBatch(String[] item) {
        return realTimeCommandDao.deleteCommandBindByBatch(item);

    }

    @Override
    public boolean addWirelessUpdateParam(WirelessUpdateParam wirelessParam) {
        return realTimeCommandDao.addWirelessUpdateParam(wirelessParam);
    }

    @Override
    public boolean addDeviceConnectServerParam(DeviceConnectServerParam connectParam) {
        return realTimeCommandDao.addDeviceConnectServerParam(connectParam);
    }

    @Override
    public WirelessUpdateParam findWirelessUpdateParam(String id) {
        return realTimeCommandDao.findWirelessUpdateParam(id);
    }

    @Override
    public DeviceConnectServerParam findDeviceConnectServerParam(String id) {
        return realTimeCommandDao.findDeviceConnectServerParam(id);
    }

    @Override
    public boolean addInformationParam(InformationParam infoParam) {
        return realTimeCommandDao.addInformationParam(infoParam);
    }

    @Override
    public boolean addEventSetParam(EventSetParam eventParam) {
        return realTimeCommandDao.addEventSetParam(eventParam);
    }

    @Override
    public boolean addPhoneBookParam(PhoneBookParam phoneBookParam) {
        return realTimeCommandDao.addPhoneBookParam(phoneBookParam);
    }

    @Override
    public boolean addStationParam(StationParam stationParam) {
        return this.realTimeCommandDao.addStationParam(stationParam);
    }

    @Override
    public StationParam getStationParam(String vehicleId, String commandType) {
        return realTimeCommandDao.getStationParam(vehicleId, commandType);
    }

    @Override
    public List<SerialPortParam> getSerialPortParam(List<String> id) {
        return realTimeCommandDao.getSerialPortParam(id);
    }

    @Override
    public StationParam findStationParam(String id) {
        return realTimeCommandDao.findStationParam(id);
    }

    @Override
    public List<EventSetParam> findEventParam(List<String> id) {
        return realTimeCommandDao.findEventParam(id);
    }

    @Override
    public List<InformationParam> findInformationParam(List<String> id) {
        return realTimeCommandDao.findInformationParam(id);
    }

    @Override
    public List<PhoneBookParam> findPhoneBookParam(List<String> id) {
        return realTimeCommandDao.findPhoneBookParam(id);
    }

    @Override
    public CommunicationParam getCommunicationParam(String id, String commandType) {
        return realTimeCommandDao.getCommunicationParam(id, commandType);
    }

    @Override
    public DeviceParam getDeviceParam(String id, String commandType) {
        return realTimeCommandDao.getDeviceParam(id, commandType);
    }

    @Override
    public GNSSParam getGNSSParam(String id, String commandType) {
        return realTimeCommandDao.getGNSSParam(id, commandType);
    }

    @Override
    public PhoneParam getPhoneParam(String id, String commandType) {
        return realTimeCommandDao.getPhoneParam(id, commandType);
    }

    @Override
    public PositionParam getPositionParam(String id, String commandType) {
        return realTimeCommandDao.getPositionParam(id, commandType);
    }

    @Override
    public CameraParam getCameraParam(String id, String commandType) {
        return realTimeCommandDao.getCameraParam(id, commandType);
    }

    @Override
    public WirelessUpdateParam getWirelessUpdateParam(String id, String commandType) {
        return realTimeCommandDao.getWirelessUpdateParam(id, commandType);
    }

    @Override
    public DeviceConnectServerParam getDeviceConnectServerParam(String id, String commandType) {
        return realTimeCommandDao.getDeviceConnectServerParam(id, commandType);
    }

    @Override
    public MonitorCommandBindForm findBind(String id, String commandType) {
        return realTimeCommandDao.findBind(id, commandType);
    }

    @Override
    public CommunicationParam findCommunicationByParamId(String id) {
        return realTimeCommandDao.findCommunicationByParamId(id);
    }

    @Override
    public List<MonitorCommandBindForm> findReferVehicle(String commandType) {
        return realTimeCommandDao.findReferVehicle(commandType, 1, "");
    }

    @Override
    public List<MonitorCommandBindForm> findReferVehicleExcept(String commandType, String vid) {
        return realTimeCommandDao.findReferVehicleExcept(commandType, vid);
    }
}
