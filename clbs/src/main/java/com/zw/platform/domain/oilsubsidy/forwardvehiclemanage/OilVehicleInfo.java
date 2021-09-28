package com.zw.platform.domain.oilsubsidy.forwardvehiclemanage;

import lombok.Data;

/**
 * @Author: zjc
 * @Description:油补车辆信息
 * @Date: create in 2020/10/12 13:45
 */
@Data
public class OilVehicleInfo {

    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     * 线路id
     */
    private String lineId;

    /**
     * 终端id
     */
    private String deviceId;

    /**
     * 油补项目-线路标识码
     */
    private String lineNo;
    /**
     * 油补项目-公司下面的子公司编号
     */
    private String subCompanyId = "0";
    /**
     * 油补项目-公司编号
     */
    private String companyId;
    /**
     * 省编码
     */
    private String provinceId;
    /**
     * 市域编码
     */
    private String cityId;
    /**
     * 第三方外部监控对象编号(油补项目-车辆编码)
     */
    private String externalVehicleId;
    /**
     * 车辆车架号
     */
    private String vehicleVin;

    private String manufacturerId;    // 注册信息-制造商ID

    private String deviceModelNumber; // 注册信息-终端型号

}
