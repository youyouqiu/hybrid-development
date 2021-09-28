package com.zw.platform.domain.vas.alram;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description:报警参数设置实体类
 * @author:wangying
 * @time:2016年12月6日 下午4:30:32
 */
@Data
@NoArgsConstructor
public class AlarmSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 平台报警
     */
    public static final int DEVICE_ALARM = 1;
    /**
     * 报警参数设置
     */
    private String id;

    /**
     * 车辆Id
     */
    private String vehicleId;

    /**
     * 报警类型id
     */
    private String alarmParameterId;

    /**
     * 报警类型id
     */
    private String alarmTypeId;

    /**
     * 参数值
     */
    private String parameterValue;

    /**
     * 报警类型名称
     */
    private String name;

    /**
     * 报警推送（0、无 1、局部 2、全局）
     */
    private Integer alarmPush;

    /**
     * 报警类型的类型
     */
    private String type;

    /**
     * 是否可下发
     */
    private String sendFlag;

    /**
     * 报警类型
     */
    private String description = "";

    /**
     * 报警参数code
     */
    private String paramCode;

    /**
     * 车辆id
     */
    private String vId;
    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 人名
     */
    private String peopleNumber;

    /**
     * 组织
     */
    private String groups;

    /**
     * 下发状态
     */
    private Integer status;

    private boolean selected;

    /**
     * 下发参数id
     */
    private String paramId;

    /**
     * 通讯类型
     */
    private String deviceType;

    /**
     * 参数设置默认值
     */
    private Integer defaultValue;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     * 判断车辆是否设置报警参数
     */
    private boolean settingUp;

    /**
     * 字典标识
     */
    private String pos;

    private String monitorType;//监控对象类型

    //状态1
    private String stateOne;

    //状态2
    private String stateTwo;

    //高电平状态
    private Integer highSignalType;

    //低电平状态
    private Integer lowSignalType;

    private Integer ignore = 0;//是否屏蔽  1 屏蔽 0 不屏蔽

    /**
     * 0:JT/T808-2013; 1:808-2013/2019共公部分; 11:JT/T808-2019
     */
    private Integer protocolType;

    /**
     * 0:平台; 1: 终端
     */
    private Integer platformOrDevice;

    public AlarmSetting(String vehicleId, String brand) {
        this.vehicleId = vehicleId;
        this.brand = brand;
    }
}
