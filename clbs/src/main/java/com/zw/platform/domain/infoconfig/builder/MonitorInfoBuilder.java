package com.zw.platform.domain.infoconfig.builder;

import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;

import java.util.Map;

/**
 * @Author: zjc
 * @Description:监控对象信息构建类
 * @Date: create in 2021/1/6 13:52
 */
public class MonitorInfoBuilder {

    public static void buildVehicleStaticData(Map<String, String> vehicleStaticData, MonitorInfo monitorInfo) {
        if (vehicleStaticData.size() > 0) {
            // 运输行业编码
            monitorInfo.setTransType(vehicleStaticData.get("transType"));
            // 车辆类型编码
            monitorInfo.setVehicleTypeCode(vehicleStaticData.get("vehicleTypeCode"));
            monitorInfo.setOwersName(vehicleStaticData.get("owersName"));
            monitorInfo.setOwersTel(vehicleStaticData.get("owersTel"));
        }
    }

    public static void buildFakeIp(String deviceType, String identification, MonitorInfo monitorInfo) {
        Integer sign = ProtocolEnum.getSignByDeviceType(deviceType);
        if (sign == ProtocolEnum.TWO) {
            monitorInfo.setFakeIp(identification);
        }
    }
}
