package com.zw.platform.util.imports.lock;

/**
 * 导入表, 用于做表锁
 * @author create by zhouzongbo on 2020/9/1.
 */
public enum ImportTable implements ImportType {
    /**
     * 车辆表
     */
    ZW_M_VEHICLE_INFO,
    /**
     * 人员表
     */
    ZW_M_PEOPLE_INFO,
    /**
     * 人员企业关联关系
     */
    ZW_C_PEOPLE_GROUP,
    /**
     * 物品表
     */
    ZW_M_THING_INFO,
    /**
     * 监控对象组织表关联关系
     */
    ZW_C_VEHICLE_GROUP,
    /**
     * 终端表
     */
    ZW_M_DEVICE_INFO,
    /**
     * 终端与组织关联关系表
     */
    ZW_M_DEVICE_GROUP,

    /**
     * sim卡表
     */
    ZW_M_SIM_CARD_INFO,
    /**
     * SIM卡与组织关联关系表
     */
    ZW_M_SIM_GROUP,
    /**
     * 分组表
     */
    ZW_M_ASSIGNMENT,
    /**
     * 分组与组织关联关系表
     */
    ZW_M_ASSIGNMENT_GROUP,
    /**
     * 分组与用户关联关系表
     */
    ZW_M_ASSIGNMENT_USER,
    /**
     * 信息配置表
     */
    ZW_M_CONFIG,
    /**
     * 分组与车辆关联关系表
     */
    ZW_M_ASSIGNMENT_VEHICLE,
    /**
     * 信息配置与从业人员
     */
    ZW_M_CONFIG_PROFESSIONALS,
    /**
     * 车辆与音视频通道设置
     */
    zw_m_video_channel_setting,
    /**
     * 信息配置与生命周期
     */
    zw_m_service_lifecycle,
    /**
     * 从业人员
     */
    ZW_M_PROFESSIONALS_INFO,
    /**
     * 从业人员与企业关联关系表
     */
    ZW_C_PROFESSIONALS_GROUP,
    /**
     * 视频参数设置
     */
    ZW_M_VIDEO_CHANNEL_SETTING,
    /**
     * 对讲信息配置表
     */
    ZW_M_INTERCOM_INFO,
    ;

    @Override
    public String value() {
        return null;
    }
}
