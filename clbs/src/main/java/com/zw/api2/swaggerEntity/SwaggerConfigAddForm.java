package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;
/***
 @Author zhengjc
 @Date 2019/2/15 16:56
 @Description 信息配置新增实体
 @version 1.0
 **/
@Data
public class SwaggerConfigAddForm {
    /**
     * 信息配置表车辆id
     */
    @ApiParam(name = "brandID", value = "信息配置表车辆id", required = true)
    private String brandID;

    /**
     * 信息配置表sim卡id
     */
    @ApiParam(name = "simID", value = "信息配置表sim卡id", required = true)
    private String simID;

    /**
     * 信息配置表设备id
     */
    @ApiParam(name = "deviceID", value = "信息配置表设备id", required = true)
    private String deviceID;

    /**
     * 信息配置车牌号
     */
    @ApiParam(name = "brands", value = "信息配置车牌号", required = true)
    private String brands;

    /**
     * 信息配置表设备编号
     */
    @ApiParam(name = "devices", value = "信息配置表设备编号", required = true)
    private String devices;

    /**
     * 信息配置表simcard卡号
     */
    @ApiParam(name = "sims", value = "信息配置表simcard卡号", required = true)
    private String sims;

    /**
     * 车辆所属企业id
     */
    @ApiParam(name = "car_groupId", value = "车辆所属企业id", required = true)
    private String car_groupId;

    /**
     * 设备所属企业id
     */
    @ApiParam(name = "device_groupId", value = "设备的所属企业id", required = true)
    private String device_groupId;

    /**
     * SIM卡所属企业
     */
    @ApiParam(name = "sim_groupId", value = "SIM卡所属企业id", required = true)
    private String sim_groupId;

    /**
     * 监控对象类型（0：车， 1：人）
     */
    @ApiParam(name = "monitorType", value = "监控对象类型（0：车， 1：人）", defaultValue = "0", required = true)
    private String monitorType;

    /**
     * 车辆类型id
     */
    @ApiParam(name = "vehicleType", value = "车辆类型id")
    private String vehicleType;

    /**
     * 设备类型
     */
    @ApiParam(name = "deviceType", defaultValue = "0", value = "设备类型（0：交通部JT/T808-2011(扩展) 1：交通部JT/T808-2013 2：移为 3：天禾 5：BDTD-SM 6：KKS 8:BSJ-A5 9:ASO  10:F3超长待机）", required = true)
    private String deviceType;

    /**
     * 计费日期(如2017-02-16)
     */
    @ApiParam(name = "billingDate", defaultValue = "2019-02-01", value = "计费日期(如2017-02-16)")
    private String billingDate;

    /**
     * 到期日期(如2017-02-16)
     */
    @ApiParam(name = "dueDate", defaultValue = "2020-02-01", value = "到期日期(如2017-02-16)")
    private String dueDate;

    /**
     * 车辆所属分组id(可添加多个分组，多个用逗号隔开)
     */
    @ApiParam(name = "citySelID", value = "车辆所属分组id(可添加多个分组，多个用逗号隔开)", required = true)
    private String citySelID;

    /**
     * 从业人员id(可添加多个分组，多个用逗号隔开)
     */
    @ApiParam(name = "professionalsID", value = "从业人员id(可添加多个分组，多个用逗号隔开)")
    private String professionalsID;
}
