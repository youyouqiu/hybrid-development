package com.zw.platform.basic.domain;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * zw_m_config
 * @author create by zhangjuan  on 2020-10-30.
 */
@Data
@NoArgsConstructor
public class ConfigDO {

    /**
     * 信息配置ID
     */
    private String id;

    /**
     * 监控对象ID
     */
    private String monitorId;

    /**
     * 终端ID
     */
    private String deviceId;

    /**
     * SIM卡ID
     */
    private String simCardId;

    /**
     * 服务周期ID
     */
    private String serviceLifecycleId;

    /**
     * 报警状态
     */
    private Integer alarmStatus;

    /**
     * 报警时间
     */
    private Date alarmTime;

    /**
     * 在线状态
     */
    private Integer onlineStatus;

    /**
     * 离线时间
     */
    private Date offlineTime;

    /**
     * 在线时间
     */
    private Date onlineTime;

    /**
     * 最后车的经度
     */
    private Double longitude;

    /**
     * 最后车的纬度
     */
    private Double latitude;

    /**
     * 速度
     */
    private Integer speed;

    /**
     * 方向
     */
    private String orientation;

    /**
     * 位置
     */
    private String location;

    /**
     * 海拔高度
     */
    private Integer altitude;

    /**
     * 是否定位（0是未定位、1是定位）
     */
    private Integer isLocation;

    /**
     * GPS时间
     */
    private Date gpsTime;

    /**
     * 最后返回时间
     */
    private Date returnTime;

    /**
     * 0点火、1是熄火
     */
    private Integer accStatus;

    private Integer flag;

    private String createDataTime;

    private String createDataUsername;

    private String updateDataTime;

    private String updateDataUsername;

    /**
     * 人ID  未使用，统一使用的monitorId
     */
    private String peopleId;

    /**
     * 物品Id  未使用，统一使用的monitorId
     */
    private String thingId;

    /**
     * 外设ID
     */
    private String peripheralsId;

    /**
     * 监控对象类型（0：车，1：人）
     */
    private String monitorType;

    /**
     * 对讲对象ID
     */
    private String intercomInfoId;

    /**
     * 车辆密码
     */
    private String vehiclePassword;

    /**
     * 入网标识:0代表未入网 1代表入网，默认是未入网
     */
    private Integer accessNetwork;

    public ConfigDO(BindDTO bindDTO) {
        if (Objects.isNull(bindDTO.getConfigId())) {
            this.id = UUID.randomUUID().toString();
            bindDTO.setConfigId(this.id);
            this.createDataUsername = SystemHelper.getCurrentUsername();
        } else {
            this.id = bindDTO.getConfigId();
            this.updateDataUsername = SystemHelper.getCurrentUsername();
        }
        this.updateDataTime = bindDTO.getUpdateBindDate();
        this.createDataTime = bindDTO.getBindDate();
        this.monitorId = bindDTO.getId();
        this.deviceId = bindDTO.getDeviceId();
        this.simCardId = bindDTO.getSimCardId();
        this.serviceLifecycleId = bindDTO.getServiceLifecycleId();
        this.flag = 1;
        this.monitorType = bindDTO.getMonitorType();
        this.intercomInfoId = bindDTO.getIntercomInfoId();
        if (StringUtils.isNotBlank(bindDTO.getVehiclePassword())) {
            this.vehiclePassword = bindDTO.getVehiclePassword();
        }
    }
}
