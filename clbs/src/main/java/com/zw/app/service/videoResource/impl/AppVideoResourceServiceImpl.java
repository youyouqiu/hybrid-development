package com.zw.app.service.videoResource.impl;

import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.service.videoResource.AppVideoResourceService;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.dto.video.DeviceVideoParamDto;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.TerminalTypeDao;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.util.common.MonitorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * app视频回放
 * 2019/11/22 16:42
 *
 * @author lijie
 * @version 1.0
 **/
@Service
@AppServerVersion
public class AppVideoResourceServiceImpl implements AppVideoResourceService {

    @Autowired
    private TerminalTypeDao terminalTypeDao;

    @Autowired
    VideoChannelSettingDao videoChannelSettingDao;

    @Autowired
    UserService userService;

    /**
     * 根据车辆id查询视频通道列表
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/videoResource/getVideoChannel" })
    public List<VideoChannelSetting> getVideoResourceChannel(String vehicleId) {
        return videoChannelSettingDao.getAppVideoChannel(vehicleId);
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_NINE, url = {
        "/clbs/app/videoResource/getAudioAndVideoParameters" })
    public AppResultBean getAudioAndVideoParameters(String monitorId) {
        BindDTO bindDTO = MonitorUtils
            .getBindDTO(monitorId, "deviceId", "deviceNumber", "simCardNumber", "simCardId", "deviceType",
                "terminalType", "terminalManufacturer");
        if (bindDTO == null) {
            return new AppResultBean(AppResultBean.SERVER_ERROR, "没有找到监控对象绑定信息");
        }
        String terminalType = bindDTO.getTerminalType();
        String terminalManufacturer = bindDTO.getTerminalManufacturer();
        TerminalTypeInfo terminalTypeInfo = terminalTypeDao.getTerminalTypeInfoBy(terminalType, terminalManufacturer);
        if (terminalTypeInfo == null) {
            return new AppResultBean(AppResultBean.SERVER_ERROR, "没有找到监控对象音视频参数");
        }
        Integer supportVideoFlag = terminalTypeInfo.getSupportVideoFlag();
        DeviceVideoParamDto deviceVideoParamDto = new DeviceVideoParamDto();
        deviceVideoParamDto.setUserUuid(userService.getCurrentUserUuid());
        deviceVideoParamDto.setMonitorId(monitorId);
        deviceVideoParamDto.setSimcardId(bindDTO.getSimCardId());
        deviceVideoParamDto.setSimcardNumber(bindDTO.getSimCardNumber());
        deviceVideoParamDto.setDeviceId(bindDTO.getDeviceId());
        deviceVideoParamDto.setDeviceNumber(bindDTO.getDeviceNumber());
        deviceVideoParamDto.setSupportVideoFlag(supportVideoFlag);
        deviceVideoParamDto.setDeviceType(bindDTO.getDeviceType());
        if (Objects.equals(supportVideoFlag, 1)) {
            Integer audioFormat = terminalTypeInfo.getAudioFormat();
            deviceVideoParamDto.setAudioFormatStr(DeviceHelper.AUDIO_FORMAT.b2p(audioFormat));
            Integer storageAudioFormat = terminalTypeInfo.getStorageAudioFormat();
            deviceVideoParamDto.setStorageAudioFormatStr(DeviceHelper.AUDIO_FORMAT.b2p(storageAudioFormat));
            Integer samplingRate = terminalTypeInfo.getSamplingRate();
            deviceVideoParamDto.setSamplingRateStr(DeviceHelper.SAMPLING_RATE.b2p(samplingRate));
            Integer storageSamplingRate = terminalTypeInfo.getStorageSamplingRate();
            deviceVideoParamDto.setStorageSamplingRateStr(DeviceHelper.SAMPLING_RATE.b2p(storageSamplingRate));
            Integer vocalTract = terminalTypeInfo.getVocalTract();
            deviceVideoParamDto.setVocalTractStr(DeviceHelper.VOCAL_TRACT.b2p(vocalTract));
            Integer storageVocalTract = terminalTypeInfo.getStorageVocalTract();
            deviceVideoParamDto.setStorageVocalTractStr(DeviceHelper.VOCAL_TRACT.b2p(storageVocalTract));
        }
        return new AppResultBean(deviceVideoParamDto);
    }
}
