package com.zw.platform.util;

import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.param.InformationParam;
import com.zw.platform.domain.param.InformationParamInfo;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouzongbo on 2019/5/27 15:21
 */
@Component
public class MessageMenuSendTimer {

    private static final Logger logger = LoggerFactory.getLogger(MessageMenuSendTimer.class);

    @Value("${mode.check}")
    private Boolean modeCheck;
    /**
     * 45队列
     */
    private static Map<String, InformationParamInfo> informationParamInfoMap = new ConcurrentHashMap<>();

    private static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(3,
        new BasicThreadFactory.Builder().namingPattern("message-menu-schedule-pool-%d").daemon(true).build());

    @PostConstruct
    public void doScheduled() {
        if (modeCheck) {
            // 仅过检模式下执行该功能
            doScheduled45();
            doScheduled60();
            doScheduled75();
            logger.info("启动信息点播");
        }
    }

    private void doScheduled45() {
        executorService.scheduleAtFixedRate(() -> {
            Collection<InformationParamInfo> values = informationParamInfoMap.values();
            if (CollectionUtils.isNotEmpty(values)) {
                for (InformationParamInfo value : values) {
                    if (value.getSendFrequency() == InformationParam.FREQUENCY_45) {
                        sendSubscribeInfo(value);
                    }
                }
            }
        }, 1, 45, TimeUnit.SECONDS);
    }

    private void doScheduled60() {
        executorService.scheduleAtFixedRate(() -> {
            Collection<InformationParamInfo> values = informationParamInfoMap.values();
            if (CollectionUtils.isNotEmpty(values)) {
                for (InformationParamInfo value : values) {
                    if (value.getSendFrequency() == InformationParam.FREQUENCY_60) {
                        sendSubscribeInfo(value);
                    }
                }
            }
        }, 1, 60, TimeUnit.SECONDS);
    }

    private void doScheduled75() {
        executorService.scheduleAtFixedRate(() -> {
            Collection<InformationParamInfo> values = informationParamInfoMap.values();
            if (CollectionUtils.isNotEmpty(values)) {
                for (InformationParamInfo value : values) {
                    if (value.getSendFrequency() == InformationParam.FREQUENCY_75) {
                        sendSubscribeInfo(value);
                    }
                }
            }
        }, 1, 75, TimeUnit.SECONDS);
    }

    private void sendSubscribeInfo(InformationParamInfo paramInfo) {
        String deviceId = paramInfo.getDeviceId();
        Integer registerDevice = DeviceHelper.getRegisterDevice(paramInfo.getVehicleId(), paramInfo.getDeviceNumber());
        if (null == registerDevice) {
            return;
        }
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, registerDevice,
            ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message information808Message = MsgUtil.get808Message(paramInfo.getSimcardNumber(),
                ConstantUtil.T808_INFO_MSG, registerDevice, paramInfo, paramInfo.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(information808Message, ConstantUtil.T808_INFO_MSG, deviceId);
    }

    public static void put(String vehicleId, Integer infoId, InformationParamInfo param) {
        vehicleId = vehicleId + "_" + infoId;
        informationParamInfoMap.put(vehicleId, param);
    }

    public static void remove(String vehicleId, Integer infoId) {
        vehicleId = vehicleId + "_" + infoId;
        informationParamInfoMap.remove(vehicleId);
    }

}
