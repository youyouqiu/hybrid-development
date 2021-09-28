package com.zw.ws.impl;

import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.monitoring.form.T808_0x8202;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.FuelTankForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.controller.UserCache;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.common.PublicVariable;
import com.zw.ws.entity.t808.oil.OilSensorParam;
import com.zw.ws.entity.t808.oil.PeripheralMessageItem;
import com.zw.ws.entity.t808.oil.SensorParam;
import com.zw.ws.entity.t808.oil.T808_0x8900;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.parameter.T808_0x8103;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by jiangxiaoqiang on 2016/11/8.
 */
@Component
public class WsOilSensorCommandService {

    @Autowired
    private ParamSendingCache paramSendingCache;

    /**
     * 油杆数据下发
     */
    public void oilRodSensorCompose(OilVehicleSetting vehicleSetting, Integer transNo, BindDTO vehicleInfo) {
        T808_0x8103 parameter = new T808_0x8103();
        if (vehicleSetting != null) {
            parameter.setParametersCount(1);
            List<ParamItem> oilSensorParams = new ArrayList<>();
            OilSensorParam oilSensorParam = new OilSensorParam();
            String oilSensorType = vehicleSetting.getOilBoxType();
            // 若邮箱type为1，则外设id为0xF341; 若为2，则外设id为0xF342
            ParamItem paramItem = new ParamItem();
            paramItem.setParamLength(56);
            if ("1".equals(oilSensorType)) {
                paramItem.setParamId(0xF341);
            } else if ("2".equals(oilSensorType)) {
                paramItem.setParamId(0xF342);
            }
            // 若邮箱type为1，则外设id为0x41; 若为2，则外设id为0x42
            if ("1".equals(oilSensorType)) {
                oilSensorParam.setParamItemId(PublicVariable.OILLEVEL_SENSOR_ONE_ID);
            } else if ("2".equals(oilSensorType)) {
                oilSensorParam.setParamItemId(PublicVariable.OILLEVEL_SENSOR_OTWO_ID);
            }
            oilSensorParam.setInertiaCompEn(
                vehicleSetting.getCompensationCanMake() != null ? vehicleSetting.getCompensationCanMake() : 1);
            oilSensorParam.setRange(StringUtils.isNotBlank(vehicleSetting.getSensorLength())
                ? Integer.parseInt(vehicleSetting.getSensorLength()) * 10 : 0);
            oilSensorParam.setSmoothing(StringUtils.isNotBlank(vehicleSetting.getFilteringFactor())
                ? Integer.parseInt(vehicleSetting.getFilteringFactor()) : 2);
            oilSensorParam.setAutoInterval(StringUtils.isNotBlank(vehicleSetting.getAutomaticUploadTime())
                ? Integer.parseInt(vehicleSetting.getAutomaticUploadTime()) : 1);
            oilSensorParam.setOutputCorrectionK(
                StringUtils.isNotBlank(vehicleSetting.getOutputCorrectionCoefficientK())
                    ? Integer.parseInt(vehicleSetting.getOutputCorrectionCoefficientK()) : 100);
            oilSensorParam.setOutputCorrectionB(
                StringUtils.isNotBlank(vehicleSetting.getOutputCorrectionCoefficientB())
                    ? Integer.parseInt(vehicleSetting.getOutputCorrectionCoefficientB()) : 100);
            oilSensorParam.setOilType(StringUtils.isNotBlank(vehicleSetting.getFuelOil())
                ? Integer.parseInt(vehicleSetting.getFuelOil()) : 1);
            // 01-长方体；02-圆柱形；03-D形；04-椭圆形；05-其他
            oilSensorParam.setMeasureFun(getShape(
                StringUtils.isNotBlank(vehicleSetting.getShape()) ? Integer.parseInt(vehicleSetting.getShape()) : 0));
            oilSensorParam.setTankSize1(
                StringUtils.isNotBlank(vehicleSetting.getBoxLength()) ? Integer.parseInt(vehicleSetting.getBoxLength())
                    : 0);
            oilSensorParam.setTankSize2(
                StringUtils.isNotBlank(vehicleSetting.getBoxLength()) ? Integer.parseInt(vehicleSetting.getWidth())
                    : 0);
            oilSensorParam.setTankSize3(
                StringUtils.isNotBlank(vehicleSetting.getBoxLength()) ? Integer.parseInt(vehicleSetting.getHeight()) :
                    0);
            oilSensorParam.setMaxAddTime(StringUtils.isNotBlank(vehicleSetting.getAddOilTimeThreshold())
                ? Integer.parseInt(vehicleSetting.getAddOilTimeThreshold()) : 0);
            oilSensorParam.setMaxAddOil(StringUtils.isNotBlank(vehicleSetting.getAddOilAmountThreshol())
                ? Integer.parseInt(vehicleSetting.getAddOilAmountThreshol()) : 0);
            oilSensorParam.setMaxDelTime(StringUtils.isNotBlank(vehicleSetting.getSeepOilTimeThreshold())
                ? Integer.parseInt(vehicleSetting.getSeepOilTimeThreshold()) : 0);
            oilSensorParam.setMaxDelOil(StringUtils.isNotBlank(vehicleSetting.getSeepOilAmountThreshol())
                ? Integer.parseInt(vehicleSetting.getSeepOilAmountThreshol()) : 0);
            paramItem.setParamValue(oilSensorParam);
            oilSensorParams.add(paramItem);
            parameter.setParamItems(oilSensorParams);
            final Map<String, String> vehicleMap = RedisHelper.getHashMap(
                    RedisKeyEnum.MONITOR_INFO.of(vehicleSetting.getVehicleId(), "deviceId", "simCardNumber"));
            String deviceId = vehicleMap.get("deviceId");
            String simcardNumber = vehicleMap.get("simCardNumber");
            //下发后需要更新外设轮询模块,调用通用应答模块的逻辑,推送用户订阅的外设轮询websocket接口
            paramSendingCache.put(SystemHelper.getCurrentUsername(), transNo, simcardNumber,
                SendTarget.getInstance(SendModule.OIL));
            //订阅推送消息
            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo, ConstantUtil.T808_DEVICE_GE_ACK,
                    1);
            SubscibeInfoCache.getInstance().putTable(info);

            info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo,
                ConstantUtil.T808_DATA_PERMEANCE_REPORT);
            SubscibeInfoCache.getInstance().putTable(info);
            paramSendingCache.put(SystemHelper.getCurrentUsername(), transNo, simcardNumber,
                    SendTarget.getInstance(SendModule.ALARM_PARAMETER_SETTING));
            T808Message message = MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_SET_PARAM, transNo, parameter,
                vehicleInfo);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
        }
    }

    public int getShape(int measureType) {
        int resultShape = 5;
        switch (measureType) {
            case PublicVariable.RECTANGLE:
                resultShape = 0x01;
                break;
            case PublicVariable.CYLINDER:
                resultShape = 0x02;
                break;
            case PublicVariable.DSHAPE:
                resultShape = 0x03;
                break;
            case PublicVariable.OVAL:
                resultShape = 0x04;
                break;
            case PublicVariable.OTHERSHAPE:
                resultShape = 0x05;
                break;
            default:
                break;
        }
        return resultShape;
    }

    /**
     * 标定下发
     */
    public void markDataCompose(Integer transNo, BindDTO vehicleInfo, List<FuelTankForm> fuelTankForm) {
        T808_0x8900<PeripheralMessageItem<SensorParam>> parameter = new T808_0x8900<>();
        parameter.setType(PublicVariable.CALIBRATION_DATA);
        if (fuelTankForm != null && fuelTankForm.size() > 0) {
            parameter.setSum(fuelTankForm.size());
            List<PeripheralMessageItem<SensorParam>> peripheralMessageItems = new ArrayList<>();
            for (FuelTankForm fuelTank : fuelTankForm) {
                PeripheralMessageItem<SensorParam> per = new PeripheralMessageItem<>();
                String sensorType = fuelTank.getTanktyp();
                // 若邮箱type为1，则外设id为0x41; 若为2，则外设id为0x42
                if ("1".equals(sensorType)) {
                    per.setSensorID(PublicVariable.OILLEVEL_SENSOR_ONE_ID);
                } else if ("2".equals(sensorType)) {
                    per.setSensorID(PublicVariable.OILLEVEL_SENSOR_OTWO_ID);
                }
                List<OilCalibrationForm> calList = fuelTank.getOilCalList();
                List<SensorParam> list = new ArrayList<>();
                if (calList != null && calList.size() > 0) {
                    for (int j = 0; j < calList.size(); j++) {
                        OilCalibrationForm oilCal = fuelTank.getOilCalList().get(j);
                        SensorParam sensorParam = new SensorParam();
                        sensorParam.setHeight(Double.parseDouble(oilCal.getOilLevelHeight()));
                        sensorParam.setSurplus(Double.parseDouble(oilCal.getOilValue()));
                        list.add(sensorParam);
                    }
                    if (calList.size() < 50) {
                        SensorParam sensorParam = new SensorParam();
                        sensorParam.setHeight(0xFFFFFFFF);
                        sensorParam.setSurplus(0xFFFFFFFF);
                        list.add(sensorParam);
                    }
                    per.setSensorSum(list.size());
                    per.setDemarcates(list);
                } else {
                    per.setSensorSum(0);
                }
                peripheralMessageItems.add(per);
            }
            parameter.setSensorDatas(peripheralMessageItems);
            if (vehicleInfo != null) {
                String deviceId = vehicleInfo.getDeviceId();
                String simcardNumber = vehicleInfo.getSimCardNumber();
                //下发后需要更新外设轮询模块,调用通用应答模块的逻辑,推送用户订阅的外设轮询websocket接口
                paramSendingCache.put(SystemHelper.getCurrentUsername(), transNo, simcardNumber,
                    SendTarget.getInstance(SendModule.OIL));
                //订阅推送消息
                SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo,
                    ConstantUtil.T808_DEVICE_GE_ACK, 1);
                SubscibeInfoCache.getInstance().putTable(info);

                info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo,
                    ConstantUtil.T808_DATA_PERMEANCE_REPORT);
                SubscibeInfoCache.getInstance().putTable(info);

                T808Message message = MsgUtil
                    .get808Message(simcardNumber, ConstantUtil.T808_PENETRATE_DOWN, transNo, parameter,
                        vehicleInfo);
                WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_PENETRATE_DOWN, deviceId);
            }
        }
    }

    /**
     * 查询车辆位置信息
     */
    public void vehicleLocationQuery(Integer transNo, BindDTO bindDTO) {
        Objects.requireNonNull(transNo, "消息流水号不能为空");
        //订阅推送消息
        final String username = SystemHelper.getCurrentUsername();
        Objects.requireNonNull(username, "用户名不能为空");
        String deviceId = bindDTO.getDeviceId();
        String deviceType = bindDTO.getDeviceType();
        String simCardNumber = bindDTO.getSimCardNumber();
        SubscibeInfo info = new SubscibeInfo(username, deviceId, transNo, ConstantUtil.T808_GPS_INFO_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        UserCache.getInstance().put(transNo.toString(), username);

        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_QUERY_LOCATION_COMMAND, transNo, null, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_QUERY_LOCATION_COMMAND, deviceId);
    }

    public void parametersTrace(T808_0x8202 form, Integer transNo, VehicleInfo vehicleInfo) {
        String deviceId = vehicleInfo.getDeviceId();
        //订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, transNo, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message message = MsgUtil
            .get808Message(vehicleInfo.getSimcardNumber(), ConstantUtil.T808_INTERIM_TRACE, transNo, form,
                vehicleInfo);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_INTERIM_TRACE, deviceId);
    }
}
