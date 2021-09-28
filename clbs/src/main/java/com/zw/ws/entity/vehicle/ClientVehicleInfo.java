package com.zw.ws.entity.vehicle;

import com.google.common.base.Stopwatch;
import com.zw.ws.entity.device.ClientDeviceInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Title: ClientVehicleInfo.java
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: Jiangxiaoqiang
 * @date 2016年7月25日下午3:58:48
 */
@Data
public class ClientVehicleInfo {
    private String vehicleId;

    private String vehicleLicense;

    private String vehiclePosition;

    private String vehicleTypeId;

    private String typeName;

    private Integer plateColorId;

    private String plateColor;

    private String vin;

    private String brand;

    private String limitNo;

    private String limitUnit;

    private String fuelType;

    private String workState;

    private String provinceId;

    private String speed;

    private String cityId;

    private List<String> groupIds = new ArrayList<>();

    private String groupName;

    private String manualList;

    private String chassisNo;

    private String engineNo;

    private String deviceNo;

    private String manufacturer;

    private Integer channelNum;

    private Integer deviceTypeId;

    private String deviceType;

    private String simCardNo;

    private String name;

    private String videoIp;

    private Integer videoPort;

    private String communicateCode;

    private String vehicleUse;

    private String installUser;

    private String installDate;

    private String installAddress;

    private String costExperireDate;

    private String photoList;

    private String ownerName;

    private String phone;

    private String idNum;

    private String email;

    private String contactName1;

    private String contactName2;

    private String contactPhone2;

    private String insuranceDate;

    private String startDate;

    private String endDate;

    private String propertyDate;

    private String remindTime;

    private String license;

    private String licenseAddress;

    private String address;

    private String postalCode;

    private String sex;

    private Integer vehicleStatus;

    private Date latestGpsDate;

    /**
     * 是否订阅了位置信息
     */
    private Integer isSubscribeLocation;

    /**
     * 是否订阅了状态消息
     */
    private Integer isSubscribeStatus;

    /**
     * 是否订阅了报警信息
     */
    private Integer isSubscribeAlarm;

    private ClientDeviceInfo deviceInfo = new ClientDeviceInfo();

    /**
     * 每台车分配一个计时器，用线程扫描计时器
     * 接收到心跳后重置计时器
     * 在相应时间后触发相应的事件
     */
    private Stopwatch stopwatch;

    private Set<String> monitorIds;
}
