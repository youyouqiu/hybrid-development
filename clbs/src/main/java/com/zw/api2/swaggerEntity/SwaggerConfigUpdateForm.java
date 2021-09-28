package com.zw.api2.swaggerEntity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.Data;


/***
 @Author zhengjc
 @Date 2019/2/1 9:18
 @Description 信息配置的表单信息
 @version 1.0
 **/

@Data
public class SwaggerConfigUpdateForm {

    /**
     * id
     */
    @ApiParam(name = "id", value = "id")
    private String id;

    /**
     * configId
     */
    @ApiParam(name = "configId", value = "信息配置表id",required = true)
    private String configId;

    /**
     * 信息配置表车辆id
     */
    @ApiParam(name = "brandID", value = "信息配置表车辆id", required = true)
    private String brandID;

    /**
     * 监控对象类型（0：车， 1：人）
     */
    @ApiParam(name = "monitorType", value = "监控对象类型（0：车， 1：人）", defaultValue = "0", required = true)
    private String monitorType;

    /**
     * 校验是否新增（仅信息配置修改功能使用）0:新增，1：修改
     */
    @ApiParam(name = "checkEdit", value = "校验是否新增（仅信息配置修改功能使用）0:新增，1：修改（修改时必填）",required = true)
    private Integer checkEdit;

    /**
     * 从业人员id(可添加多个分组，多个用逗号隔开)
     */
    @ApiParam(name = "professionalsID", value = "从业人员id(可添加多个分组，多个用逗号隔开)")
    private String professionalsID;

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
     * 车辆企业名称
     */
    @ApiParam(name = "carGroupName", value = "车辆的企业名称",required = true)
    private String carGroupName;

    /**
     * 车辆的企业id
     */
    @ApiParam(name = "carGroupId", value = "车辆的企业id",required = true)
    private String carGroupId;

    /**
     * 车主电话
     */
    @ApiParam(name = "vehicleOwnerPhone", value = "车辆的企业id")
    private String vehicleOwnerPhone;

    /**
     * 车辆类型
     */
    @ApiParam(name = "vehicleType", value = "车辆类型")
    private String vehicleType;

    /**
     * 物品类型
     */
    @ApiParam(name = "thingType", value = "物品类型")
    private String thingType;

    /**
     * 分组id
     */
    @ApiParam(name = "groupid", value = "分组id",required = true)
    private String groupid;

    /**
     * 企业名称
     */
    @ApiParam(name = "groupName", value = "企业名称",required = true)
    private String groupName;

    /**
     * 终端id
     */
    @ApiParam(name = "deviceID", value = "终端id",required = true)
    private String deviceID;

    /**
     * 终端企业名称
     */
    @ApiParam(name = "deviceGroupName", value = "终端企业名称",required = true)
    private String deviceGroupName;

    /**
     * 终端所属企业id
     */
    @ApiParam(name = "终端所属企业id", value = "终端所属企业id",required = true)
    private String deviceGroupId;


    /**
     *终端协议类型
     */
    @ApiParam(name = "deviceType", value = "终端协议类型",required = true)
    private String deviceType;
    /**
     * 终端功能类型
     */
    @ApiParam(name = "functionalType", value = "终端功能类型",required = true)
    private String functionalType;

    /**
     * sim卡所属企业名称
     */
    @ApiParam(name = "iccidSim", value = "sim卡所属企业名称")
    private String iccidSim;

    /**
     * sim卡所属企业id
     */
    @ApiParam(name = "simParentGroupId", value = "sim卡所属企业id",required = true)
    private String simParentGroupId;

    /**
     * 运营商
     */
    @ApiParam(name = "operator", value = "运营商")
    private String operator;

    /**
     * 服务周期ID
     */
    @ApiParam(name = "serviceLifecycleId", value = "服务周期ID")
    private String serviceLifecycleId;

    /**
     * 计费日期
     */
    @ApiParam(name = "billingDateStr", value = "计费日期")
    private String billingDateStr;

    /**
     * 到期日期
     */
    @ApiParam(name = "durDateStr", value = "到期日期")
    private String durDateStr;

    /**
     * 外设ID
     */
    @ApiParam(name = "peripheralsId", value = "外设ID")
    private String peripheralsId;



}
