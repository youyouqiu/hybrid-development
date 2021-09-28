package com.zw.platform.domain.infoconfig.builder;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.infoconfig.form.Config1Form;
import com.zw.platform.domain.infoconfig.form.ConfigImportForm;
import com.zw.platform.domain.infoconfig.form.ConfigTransportImportForm;
import com.zw.platform.domain.netty.BindInfo;
import com.zw.platform.util.BSJFakeIPUtil;

/**
 * @Author: zjc
 * @Description:绑定信息构建类
 * @Date: create in 2021/1/6 16:36
 */
public class BindInfoBuilder {

    public static void buildVehicleInfo(BindInfo bindInfo, VehicleInfo vehicle) {
        bindInfo.setOldDeviceId(vehicle.getDeviceId());
        bindInfo.setDeviceId(vehicle.getDeviceId());
        bindInfo.setFakeIp(BSJFakeIPUtil.integerMobileIPAddress(vehicle.getSimcardNumber()));
        bindInfo.setDeviceNumber(vehicle.getDeviceNumber());
        bindInfo.setDeviceType(vehicle.getDeviceType());
        bindInfo.setSimCard(vehicle.getSimcardNumber());
        String identify = buildIdentify(vehicle.getDeviceType(), vehicle.getSimcardNumber(), vehicle.getDeviceNumber());
        buildIdentification(bindInfo, identify);
    }

    private static void buildIdentification(BindInfo bindInfo, String identification) {
        bindInfo.setOldIdentification(identification);
        bindInfo.setIdentification(identification);
    }

    public static String buildIdentify(String deviceType, String simCardNumber, String deviceNumber) {
        Integer sign = ProtocolEnum.getSignByDeviceType(deviceType);
        String identification = "";
        if (sign == ProtocolEnum.ONE) {
            identification = simCardNumber;
        } else if (sign == ProtocolEnum.TWO) {
            //将sim卡号转换为伪IP
            identification = BSJFakeIPUtil.integerMobileIPAddress(simCardNumber);
        } else {
            identification = deviceNumber;
        }
        return identification;
    }

    public static void buildIdentify(BindInfo bindInfo, Config1Form config1Form) {
        String identify =
            buildIdentify(config1Form.getDeviceType(), config1Form.getSims(), config1Form.getDevices());
        buildIdentification(bindInfo, identify);
    }

    public static void buildIdentify(BindInfo bindInfo, ConfigTransportImportForm cfg) {
        String identify = buildIdentify(cfg.getDeviceType(), cfg.getSimcardNumber(), cfg.getDeviceNumber());
        buildIdentification(bindInfo, identify);
    }

    public static void buildIdentify(BindInfo bindInfo, ConfigImportForm cfg) {
        String identify = buildIdentify(cfg.getDeviceType(), cfg.getSimcardNumber(), cfg.getDeviceNumber());
        buildIdentification(bindInfo, identify);
    }



}
