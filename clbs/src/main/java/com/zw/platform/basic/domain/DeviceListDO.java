package com.zw.platform.basic.domain;

import lombok.Data;

import java.util.Date;

/**
 * @Author: zjc
 * @Description:终端列表信息,与数据库对应
 * @Date: create in 2020/10/29 11:11
 */
@Data
public class DeviceListDO {

    /**
     * 终端厂商
     */
    private String terminalManufacturer;

    /**
     * 安装日期（yyyy-MM-dd）
     */
    private Date installTime;

    /**
     * 制造商
     */
    private String manuFacturer;

    /**
     * 终端型号（注册）
     */
    private String deviceModelNumber;

    /**
     * 备注
     */
    private String remark;

    /**
     * 终端号
     */
    private String deviceNumber;

    /**
     * 终端名称
     */
    private String deviceName;

    /**
     * 1:简易型车机；2：行车记录仪； 3：对讲设备；4：手咪设备(需要转换一下)
     */
    private String functionalType;

    /**
     * 是否符合要求（1:是 0:否）
     */
    private Integer complianceRequirements;

    /**
     * 采购日期（yyyy-MM-dd）
     */
    private Date procurementTime;

    /**
     * 终端id
     */
    private String id;

    /**
     * 通讯类型
     */
    private String deviceType;

    /**
     * 安装单位
     */
    private String installCompany;

    /**
     * 修改日期
     */
    private Date updateDataTime;

    /**
     * 注册信息-制造商ID
     */
    private String manufacturerId;

    /**
     * 联系方式
     */
    private String telephone;

    /**
     * 启停状态0:停用 1:启用
     */
    private Integer isStart;

    /**
     * 条码
     */
    private String barCode;

    /**
     * 终端型号
     */
    private String terminalType;

    /**
     * 企业id
     */
    private String orgId;

    /**
     * mac地址
     */
    private String macAddress;

    /**
     * 创建日期
     */
    private Date createDataTime;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * 监控对象
     */
    private String monitorId;

    /**
     * 通道数
     */
    private Integer channelNumber;

    /**
     * 是否视频：0是 1否
     */
    private Integer isVideo;

    /**
     * 鉴权码
     */
    private String authCode;

}
