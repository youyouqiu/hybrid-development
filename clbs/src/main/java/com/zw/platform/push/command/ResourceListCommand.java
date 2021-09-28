package com.zw.platform.push.command;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.realTimeVideo.ResourceListSend;
import com.zw.platform.domain.reportManagement.form.LogSearchForm;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.factory.AlarmChainHandler;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.Customer;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

import static com.zw.platform.util.DateUtil.YMD_HMS_20;
import static com.zw.platform.util.DateUtil.fromTimestamp;

/**
 * 联动策略 -> 上传音视频资源列表
 * @author create by denghuabing on xxx.
 */

@Slf4j
@Component
public class ResourceListCommand implements AlarmChainHandler {

    private static final int ONE_HOUR = 60 * 60 * 1000;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Override
    public void handle(AlarmMessageDTO alarmMessageDTO) {
        final String vehicleId = alarmMessageDTO.getMonitorId();
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindDTO == null) {
            return;
        }
        // 终端ID
        String deviceId = bindDTO.getDeviceId();
        String deviceType = bindDTO.getDeviceType();
        String mobile = bindDTO.getSimCardNumber();
        // 组装下发参数所需要的实体
        final Long startAlarmTime = alarmMessageDTO.getStartAlarmTime();
        ResourceListSend send = getResourceList(startAlarmTime);
        Integer msgSn = Integer.valueOf(new Customer().getCustomerID());
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSn,
                ConstantUtil.VIDEO_DOWN_EXG_MSG_RETURN_RESOURCE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message t808Message =
            MsgUtil.get808Message(mobile, ConstantUtil.T809_DOWN_EXG_MSG_RETURN_STARTUP, msgSn, send, deviceType);
        WebSubscribeManager.getInstance()
                .sendMsgToAll(t808Message, ConstantUtil.T809_DOWN_EXG_MSG_RETURN_STARTUP, deviceId);
        String brand = bindDTO.getName();
        LogSearchForm form = new LogSearchForm();
        form.setEventDate(new Date());
        form.setLogSource("3");
        form.setModule("REALTIMEVIDEO");
        form.setBrand(brand);
        form.setGroupId(bindDTO.getOrgId());
        form.setMessage("监控对象(" + brand + ")查询资源列表");
        final Object plateColor = bindDTO.getPlateColor();
        if (plateColor != null) {
            form.setPlateColor(Integer.parseInt(plateColor.toString()));
        }
        logSearchServiceImpl.addLogBean(form);
    }

    /**
     * 组装资源列表下发数据
     * @param startAlarmTime 开始报警时间
     * @return ResourceListSend
     */
    private ResourceListSend getResourceList(Long startAlarmTime) {
        ResourceListSend send = new ResourceListSend();
        // 所有通道号
        send.setChannelNum(0);
        // 查询开始时间和结束时间为报警开始时间或则当前时间的前后一个小时.
        if (startAlarmTime != null) {
            long startTime = startAlarmTime - ONE_HOUR;
            long endTime = startTime + ONE_HOUR;
            send.setStartTime(YMD_HMS_20.format(fromTimestamp(startTime)).orElseThrow(IllegalArgumentException::new));
            send.setEndTime(YMD_HMS_20.format(fromTimestamp(endTime)).orElseThrow(IllegalArgumentException::new));
        } else {
            final LocalDateTime now = LocalDateTime.now();
            final LocalDateTime startTime = now.minusDays(1);
            final LocalDateTime endTime = now.plusHours(1);
            send.setStartTime(YMD_HMS_20.format(startTime).orElseThrow(IllegalArgumentException::new));
            send.setEndTime(YMD_HMS_20.format(endTime).orElseThrow(IllegalArgumentException::new));
        }
        send.setAlarm(0);
        // 0：所有存储器
        send.setStorageType(0);
        // 0:所有码流
        send.setStreamType(0);
        send.setVideoType(0);
        return send;
    }
}
