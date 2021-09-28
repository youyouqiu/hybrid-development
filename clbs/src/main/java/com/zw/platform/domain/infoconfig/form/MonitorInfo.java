package com.zw.platform.domain.infoconfig.form;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 监控对象实体
 * @author hujun
 * @date 2018/6/20 9:50
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MonitorInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer monitorType; //监控对象类型 0：车 1：人 2：物

    private String monitorName; //监控对象名称

    private String monitorId; //监控对象id

    private String fuelType; //燃油类型

    private String groupId; //所属企业id

    private String groupName; //所属企业名称

    private String assignmentName; //分组名称

    private String assignmentId; //分组id

    private String deviceId; //终端id

    private String deviceNumber; //终端编号

    private String fakeIp; //伪IP

    private String deviceType; //终端协议类型

    private String professionalsName; //从业人员名称

    private String simcardNumber; //sim卡号

    private String identity; //身份证号

    private Integer plateColor; //车牌颜色

    private String plateColorName;//

    private String monitorTypeIconl; //监控对象类型图标

    private String monitorTypeIcon; //监控对象类型图标 对应 redis 最后一条位置信息存的名字

    private String monitorIcon; //监控对象图标

    private String label; //品牌

    private String model; //型号

    private String material; //材料

    private String weight; //重量

    private String spec; //规格

    private String productDate; //生产日期

    private String authCode; //鉴权码

    private String vehicleNumber; //车辆编号

    private String vehicleType; //车辆类型

    private String vehicleTypeCode; // 车辆类型编码

    private String transType; // 车辆营运类别

    private String owersName;// 业户名称

    private String owersTel; // 业户电话

    /**
     * 安装时间
     */
    private Long installTime;
    /**
     * 安装单位
     */
    private String installCompany;
    /**
     * 联系电话
     */
    private String telephone;
    /**
     * 联系人
     */
    private String contacts;
    /**
     * 是否符合要求，0：否，1：是
     */
    private Integer complianceRequirements;

    /**
     * 车主
     */
    private String vehicleOwner;

    /**
     * 车主电话
     */
    private String vehicleOwnerPhone;

    /**
     * 报警记录从业人员名称
     */
    private String alarmProfessionalsName;

    /**
     * 从业人员电话
     */
    private String phone;

    /**
     * 从业人员电话2
     */
    private String phoneTwo;

    /**
     * 从业人员电话3
     */
    private String phoneThree;

    /**
     * 终端厂商
     */
    private String terminalManufacturer;

    /**
     * 终端型号
     */
    private String terminalType;

    /**
     * 车辆省域ID--所属省份的前两位行政区划代码
     */
    private String provinceId;

    /**
     * 市域ID--所属城市的后4位行政区划代码 provinceId+cityId=车辆所属省市区的行政区划代码
     */
    private String cityId;

    /**
     * 入网标识（0代表未入网，1带表入网）
     */
    private Integer accessNetwork;
}
