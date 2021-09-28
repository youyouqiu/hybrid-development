package com.zw.platform.basic.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 终端实体
 * @author wangying
 */
@Data
public class DeviceInfoDo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Id
     */
    private String id;

    /**
     * 终端编号
     */
    private String deviceNumber;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 启停状态
     */
    private Integer isStart;

    /**
     * 设备类型:0:交通部JT/T808-2011(扩展);1:交通部JT/T808-2013;2:移为GV320;3:天禾;5:北斗天地协议;8:博实结;9:ASO;10:F3超长待机;11:808-2019
     */
    private String deviceType;

    /**
     * 通道数
     */
    private Integer channelNumber;

    /**
     * 是否视频
     */
    private Integer isVideo;

    /**
     * 条码
     */
    private String barCode;

    /**
     * 制造商
     */
    private String manuFacturer;

    /**
     * 逻辑删除标志
     */
    private Integer flag;

    /**
     * 安装时间
     */
    private Date installTime;

    /**
     * 创建时间
     */
    private Date createDataTime;

    /**
     * 采购时间
     */
    private Date procurementTime;

    /**
     * 备注
     */
    private String remark;
    /**
     * 创建用户
     */
    private String createDataUsername;

    /**
     * 更新时间
     */
    private Date updateDataTime;

    /**
     * 更新用户
     */
    private String updateDataUsername;

    /**
     * 设备功能类型
     */
    private String functionalType;

    /**
     * 鉴权码
     */
    private String authCode;

    /**
     * 注册信息-制造商ID
     */
    private String manufacturerId;

    /**
     * 注册信息-终端型号
     */
    private String deviceModelNumber;

    /**
     * 终端厂商
     */
    private String terminalManufacturer;

    /**
     * 终端型号id
     */
    private String terminalTypeId;

    /**
     * 终端型号
     */
    private String terminalType;

    /**
     * mac地址
     */
    private String macAddress;

    /**
     * 企业id
     */
    private String orgId;
    /**
     * 绑定id,用户修改页面显示红色提示语
     */
    private String bindId;
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 安装单位
     */
    private String installCompany;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * 是否符合要求（1:是 0:否）
     */
    private Integer complianceRequirements;

    /**
     * 联系方式
     */
    private String telephone;
}
