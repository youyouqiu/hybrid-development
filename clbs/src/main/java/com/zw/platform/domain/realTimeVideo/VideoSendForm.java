package com.zw.platform.domain.realTimeVideo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 视频指令下发参数实体
 * @author hujun
 *
 */
@Data
public class VideoSendForm implements Serializable {
    private static final long serialVersionUID = 1L;
    //公用字段
    private String vehicleId;//车辆id
    private String brand;//车牌号
    private String deviceId;//终端id
    private String deviceNumber;//终端编号
    private String simNumber;//sim卡号
    private int orderType;//指令类型
    private Integer serialNumber;//流水号
    private String channelNum;//逻辑通道号
    private Integer operationIssueType;//操作下发类型 0：用户操作 1：联动策略操作
    private Integer channelType; // 0 音视频； 1：音频； 2：视频
    private String streamType;//码流类型（0：主码流，1：子码流）
    private Integer requestType; // 请求方式 0实时视频 1资源回放
    
    //视频操作下发（打开视频、关闭视频、清除视频、暂停视频、清除视频、主子码流切换、静音）
    private String videoParams; 
    private List<VideoBasicsParam> videoBasicsParams;
    
    //通道号设置
    private Integer audioSum;//音频通道总数
    private Integer videoSum;//视频通道总数
    private Integer audioVideoSum;//音视频通道总数
    private String contrasts;//通道对照数据
    
    //设置休眠唤醒
    private Integer wakeupHandSign;//手动唤醒模式0：禁用，1：启用
    private Integer wakeupConditionSign;//条件唤醒模式0：禁用，1：启用
    private Integer wakeupTimeSign;//定时唤醒模式0：禁用，1：启用
    private Integer wakeupCondition;//唤醒条件类型
    private Integer wakeupTime;//定时唤醒日设置
    private Integer wakeupTimeFlag;//定时唤醒启用标志
    private String wakeupTime1;//时间段1唤醒时间
    private String wakeupClose1;//时间段1关闭时间
    private String wakeupTime2;//时间段2唤醒时间
    private String wakeupClose2;//时间段2关闭时间
    private String wakeupTime3;//时间段3唤醒时间
    private String wakeupClose3;//时间段2关闭时间
    private String wakeupTime4;//时间段4唤醒时间
    private String wakeupClose4;//时间段2关闭时间
    
    //休眠唤醒
    private Integer msgId;//消息id
    private String awakenTime;//唤醒时长

    private String monitorType;// 监控对象类型

    /**
     * 终端类型
     */
    private String deviceType;
}
