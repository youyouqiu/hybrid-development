package com.zw.ws.impl;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.t808.oil.WorkHourParam;
import com.zw.ws.entity.t808.oil.WorkHourSettingParam;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.parameter.T808_0x8103;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.zw.platform.push.cache.SendModule.LOAD;
import static com.zw.platform.push.cache.SendModule.TIME;

/**
 * Created by LiaoYuecai on 2016/9/27.
 */
@Log4j2
@Component
public class SensorService {
    private static final Logger logger = LogManager.getLogger(SensorService.class);

    @Autowired
    ParamSendingCache paramSendingCache;

    private static final int SHAKE_SENSOR_ID = 0xF35A;// 震动传感器基准ID（完整ID为基准ID+传感器ID）

    private static final int SHAKE_SENSOR_PARAM_LEN = 56;

    private static final int WORK_HOUR_SENSOR_PARAM_LEN = 56;

    /**
     * 1发动机  （完整ID为基准ID(0xf3)+传感器ID(80)）  8103: 其中id为DWORD所以于鏊加一个f3 协议需要传递0xf3
     */
    public static final int WORK_HOUR_SENSOR_SEQUENCE_ID = 0xf380;
    /**
     * 2发动机
     */
    public static final int WORK_HOUR_SENSOR_SEQUENCE_TWO_ID = 0xf381;

    public void sendSensorParam(int type, VehicleInfo vehicleInfo, Object obj, Integer transNo) {
        if (type == 0x01) {
            try {
                shakeParam(vehicleInfo, (VibrationSensorBind) obj, transNo);
            } catch (Exception e) {
                logger.error("SensorService类异常" + e + "," + vehicleInfo + "," + obj + "," + transNo);
            }
        }
    }

    /**
     * 下发震动传感器参数（下发参数这块代码比较乱，后期需要优化，重复代码太多，所有参数下发可以写在同一个类中，下发接口也需要优化）
     */
    private void shakeParam(VehicleInfo vehicleInfo, VibrationSensorBind sensor, Integer transNo) {
        T808_0x8103 benchmark = new T808_0x8103();
        ParamItem paramItem = new ParamItem();
        paramItem.setParamLength(SHAKE_SENSOR_PARAM_LEN);
        paramItem.setParamId(SHAKE_SENSOR_ID);
        WorkHourParam workHourParam = new WorkHourParam();
        if (sensor != null) {
            workHourParam.setInertiaCompEn(sensor.getInertiaCompEn() != null ? sensor.getInertiaCompEn() : 1);
            workHourParam.setFilterFactor(sensor.getFilterFactor() != null ? sensor.getFilterFactor() : 2);
            workHourParam.setUploadTime(
                StringUtils.isNotBlank(sensor.getUploadTime()) ? Integer.parseInt(sensor.getUploadTime()) : 1);
            workHourParam.setOutputCorrectionK(
                StringUtils.isNotBlank(sensor.getOutputCorrectionK()) ? Integer.parseInt(sensor.getOutputCorrectionK())
                    : 100);
            workHourParam.setOutputCorrectionB(
                StringUtils.isNotBlank(sensor.getOutputCorrectionB()) ? Integer.parseInt(sensor.getOutputCorrectionB())
                    : 100);
            workHourParam.setOutageFrequencyThreshold(StringUtils.isNotBlank(sensor.getOutageFrequencyThreshold())
                ? Integer.parseInt(sensor.getOutageFrequencyThreshold()) : 0);
            workHourParam.setContinueOutageTimeThreshold(
                StringUtils.isNotBlank(sensor.getContinueOutageTimeThreshold())
                    ? Integer.parseInt(sensor.getContinueOutageTimeThreshold()) : 0);
            workHourParam.setWorkFrequencyThreshold(StringUtils.isNotBlank(sensor.getWorkFrequencyThreshold())
                ? Integer.parseInt(sensor.getWorkFrequencyThreshold()) : 0);
            workHourParam.setIdleFrequencyThreshold(StringUtils.isNotBlank(sensor.getIdleFrequencyThreshold())
                ? Integer.parseInt(sensor.getIdleFrequencyThreshold()) : 0);
            workHourParam.setContinueIdleTimeThreshold(StringUtils.isNotBlank(sensor.getContinueIdleTimeThreshold())
                ? Integer.parseInt(sensor.getContinueIdleTimeThreshold()) : 0);
            workHourParam.setContinueWorkTimeThreshold(StringUtils.isNotBlank(sensor.getContinueWorkTimeThreshold())
                ? Integer.parseInt(sensor.getContinueWorkTimeThreshold()) : 0);
            workHourParam.setContinueAlarmTimeThreshold(StringUtils.isNotBlank(sensor.getContinueAlarmTimeThreshold())
                ? Integer.parseInt(sensor.getContinueAlarmTimeThreshold()) : 0);
            workHourParam.setCollectNumber(sensor.getCollectNumber() != null ? sensor.getCollectNumber() : 0);
            workHourParam.setUploadNumber(sensor.getUploadNumber() != null ? sensor.getUploadNumber() : 0);
            workHourParam.setAlarmFrequencyThreshold(StringUtils.isNotBlank(sensor.getAlarmFrequencyThreshold())
                ? Integer.parseInt(sensor.getAlarmFrequencyThreshold()) : 0);
        }
        paramItem.setParamValue(workHourParam);
        benchmark.getParamItems().add(paramItem);
        benchmark.setParametersCount(benchmark.getParamItems().size());
        try {
            if (StringUtils.isNotBlank(vehicleInfo.getSimcardNumber())) {
                // 订阅消息
                getSubscibeInfo(vehicleInfo, transNo, benchmark, null);
            }
        } catch (Exception e) {
            logger.error("SensorService类异常shakeParam" + e);
        }
    }

    /**
     * 下发工时设置信息
     * @param type        type
     * @param vehicleInfo vehicleInfo
     * @param info        info
     * @param transNo     transNo
     * @param mark        0:常规参数下发/参数下发; 1:基值修正下发
     */
    public void sendWorkHourSensorParam(int type, BindDTO vehicleInfo, WorkHourSettingInfo info, Integer transNo,
        int mark) {
        if (type == 0x01) {
            try {
                workHourSettingParam(vehicleInfo, info, transNo, mark);
            } catch (Exception e) {
                logger.error("SensorService类异常" + e + "," + vehicleInfo + "," + info + "," + transNo);
            }
        }
    }

    private void workHourSettingParam(BindDTO vehicleInfo, WorkHourSettingInfo sensor, Integer transNo,
        Integer mark) {
        T808_0x8103 benchmark = new T808_0x8103();
        ParamItem paramItem = new ParamItem();
        paramItem.setParamLength(WORK_HOUR_SENSOR_PARAM_LEN);
        if (sensor.getSensorSequence() == WorkHourSettingInfo.SENSOR_SEQUENCE_ONE) {
            paramItem.setParamId(WORK_HOUR_SENSOR_SEQUENCE_ID);
        } else {
            paramItem.setParamId(WORK_HOUR_SENSOR_SEQUENCE_TWO_ID);
        }
        WorkHourSettingParam workHourParam = getWorkHourSettingParam(sensor);
        paramItem.setParamValue(workHourParam);
        benchmark.getParamItems().add(paramItem);
        benchmark.setParametersCount(benchmark.getParamItems().size());
        try {
            if (StringUtils.isNotBlank(vehicleInfo.getSimCardNumber())) {
                // 订阅消息
                getSubscibeInfo(vehicleInfo, transNo, benchmark, TIME);
            }
        } catch (Exception e) {
            logger.error("SensorService类异常workHourSettingParam" + e);
        }
    }

    private WorkHourSettingParam getWorkHourSettingParam(WorkHourSettingInfo sensor) {
        WorkHourSettingParam workHourParam = new WorkHourSettingParam();
        workHourParam.setCompensate(sensor.getCompensate() != null ? sensor.getCompensate() : 1);
        workHourParam.setUploadTime(
            StringUtils.isNotBlank(sensor.getUploadTime()) ? Integer.parseInt(sensor.getUploadTime()) : 1);
        workHourParam.setOutputCorrectionK(
            StringUtils.isNotBlank(sensor.getOutputCorrectionK()) ? Integer.parseInt(sensor.getOutputCorrectionK()) :
                100);
        workHourParam.setOutputCorrectionB(
            StringUtils.isNotBlank(sensor.getOutputCorrectionB()) ? Integer.parseInt(sensor.getOutputCorrectionB()) :
                100);
        Integer filterFactor = sensor.getFilterFactor();
        workHourParam.setSmoothing(Objects.nonNull(filterFactor) ? filterFactor : 1);

        int detectionMode = Objects.nonNull(sensor.getDetectionMode()) ? sensor.getDetectionMode() : 1;
        // 协议： 00：电压比较式；0x01：油耗阈值式；0x02：油耗波动式
        workHourParam.setWorkInspectionMethod(detectionMode - 1);

        if (detectionMode == 1) {
            //1: 电压比较式
            String thresholdVoltage = sensor.getThresholdVoltage();
            workHourParam.setThreshOne(
                StringUtils.isNotBlank(thresholdVoltage) ? (int) (Double.parseDouble(thresholdVoltage) * 10) : 0xFFFF);
        } else if (detectionMode == 2) {
            //2:油耗阈值式
            String threshold = sensor.getThreshold();
            workHourParam
                .setThreshOne(StringUtils.isNotBlank(threshold) ? (int) (Double.parseDouble(threshold) * 100) : 0xFFFF);
        } else {
            workHourParam.setThreshOne(0xFFFF);
        }

        // 油耗波动式
        if (detectionMode == 3) {
            Integer baudRateCalculateNumber = sensor.getBaudRateCalculateNumber();
            workHourParam.setWaveNum(Objects.nonNull(baudRateCalculateNumber) ? baudRateCalculateNumber : 8);
            Integer baudRateCalculateTimeScope = sensor.getBaudRateCalculateTimeScope();
            workHourParam.setWaveTime(Objects.nonNull(baudRateCalculateTimeScope) ? baudRateCalculateTimeScope : 4);
            Integer smoothingFactor = sensor.getSmoothingFactor();
            workHourParam.setSmoothParam(Objects.nonNull(smoothingFactor) ? smoothingFactor : 15);
        } else {
            // 电压比较式/油耗阈值式,无这些值
            workHourParam.setWaveNum(0xFF);
            workHourParam.setWaveTime(0xFF);
            if (detectionMode == 2) {
                // 油耗阈值式，需要组装平滑系数
                Integer smoothingFactor = sensor.getSmoothingFactor();
                workHourParam.setSmoothParam(Objects.nonNull(smoothingFactor) ? smoothingFactor : 15);
            } else {
                workHourParam.setSmoothParam(0xFF);
            }
        }

        Integer lastTime = sensor.getLastTime();
        workHourParam.setLastTime(Objects.nonNull(lastTime) ? lastTime : 10);
        Integer sensorSequence = sensor.getSensorSequence();
        workHourParam.setSensorSequence(Objects.nonNull(sensorSequence) ? sensorSequence : 0);
        return workHourParam;
    }

    public void getSubscibeInfo(VehicleInfo vehicleInfo, Integer transNo, T808_0x8103 benchmark,
        SendModule sendModuleType) {
        SubscibeInfo subscibeInfo =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), vehicleInfo.getDeviceId(), transNo,
                ConstantUtil.T808_DEVICE_GE_ACK, 1);
        SubscibeInfoCache.getInstance().putTable(subscibeInfo);
        subscibeInfo = new SubscibeInfo(SystemHelper.getCurrentUsername(), vehicleInfo.getDeviceId(), transNo,
            ConstantUtil.T808_DATA_PERMEANCE_REPORT);
        SubscibeInfoCache.getInstance().putTable(subscibeInfo);

        T808Message message = MsgUtil
            .get808Message(vehicleInfo.getSimcardNumber(), ConstantUtil.T808_SET_PARAM, transNo, benchmark,
                vehicleInfo.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, vehicleInfo.getDeviceId());
        String userName = SystemHelper.getCurrentUsername();
        // websocket下发订阅消息
        if (sendModuleType == null) {
            return;
        }
        switch (sendModuleType) {
            case TIME:
                paramSendingCache.put(userName, transNo, vehicleInfo.getSimcardNumber(), SendTarget.getInstance(TIME));
                break;
            case LOAD:
                paramSendingCache.put(userName, transNo, vehicleInfo.getSimcardNumber(), SendTarget.getInstance(LOAD));
                break;
            default:
                break;
        }
    }

    public void sendLoadSensorParam(BindDTO vehicle, Object loadVehicleSettingInfo, Integer transNo, int mark) {
        try {
            loadSettingParam(vehicle, loadVehicleSettingInfo, transNo, mark);
        } catch (Exception e) {
            logger.error("SensorService类异常" + e + "," + vehicle + "," + loadVehicleSettingInfo + "," + transNo);
        }
    }

    private void loadSettingParam(BindDTO vehicle, Object loadReceiveParam, Integer transNo, int mark) {
        T808_0x8103 benchmark = new T808_0x8103();
        ParamItem paramItem = new ParamItem();
        paramItem.setParamId(mark);
        paramItem.setParamLength(WORK_HOUR_SENSOR_PARAM_LEN);
        paramItem.setParamValue(loadReceiveParam);
        benchmark.getParamItems().add(paramItem);
        benchmark.setParametersCount(benchmark.getParamItems().size());
        try {
            if (StringUtils.isNotBlank(vehicle.getSimCardNumber())) {
                getSubscibeInfo(vehicle, transNo, benchmark, LOAD);
            }
        } catch (Exception e) {
            logger.error("SensorService类异常workHourSettingParam" + e);
        }
    }

    public void getSubscibeInfo(BindDTO vehicleInfo, Integer transNo, T808_0x8103 benchmark,
        SendModule sendModuleType) {
        SubscibeInfo subscibeInfo =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), vehicleInfo.getDeviceId(), transNo,
                ConstantUtil.T808_DEVICE_GE_ACK, 1);
        SubscibeInfoCache.getInstance().putTable(subscibeInfo);
        subscibeInfo = new SubscibeInfo(SystemHelper.getCurrentUsername(), vehicleInfo.getDeviceId(), transNo,
            ConstantUtil.T808_DATA_PERMEANCE_REPORT);
        SubscibeInfoCache.getInstance().putTable(subscibeInfo);

        T808Message message = MsgUtil
            .get808Message(vehicleInfo.getSimCardNumber(), ConstantUtil.T808_SET_PARAM, transNo, benchmark,
                vehicleInfo);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, vehicleInfo.getDeviceId());
        String userName = SystemHelper.getCurrentUsername();
        // websocket下发订阅消息
        if (sendModuleType == null) {
            return;
        }
        switch (sendModuleType) {
            case TIME:
                paramSendingCache.put(userName, transNo, vehicleInfo.getSimCardNumber(), SendTarget.getInstance(TIME));
                break;
            case LOAD:
                paramSendingCache.put(userName, transNo, vehicleInfo.getSimCardNumber(), SendTarget.getInstance(LOAD));
                break;
            default:
                break;
        }
    }
}
