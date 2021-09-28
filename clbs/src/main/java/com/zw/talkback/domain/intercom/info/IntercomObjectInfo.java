package com.zw.talkback.domain.intercom.info;

import lombok.Data;

import java.io.Serializable;

/**
 * 对讲对象查询数据返回
 * @author zhouzongbo on 2019/8/6 16:36
 */
@Data
public class IntercomObjectInfo implements Serializable {

    private static final long serialVersionUID = -1119194811869613208L;

    /**
     * 我们平台的对讲对象管理ID
     */
    private String id;

    /**
     * 对讲平台存储的对讲对象ID
     */
    private Long userId;

    /**
     * 对象ID
     */
    private String monitorId;

    /**
     * SIM卡ID
     */
    private String simcardId;

    /**
     * 信息配置表ID
     */
    private String configId;

    /**
     * 原始机型ID(index)
     */
    private String originalModelId;

    /**
     * 所属组织ID
     */
    private String groupId;

    /**
     * 监控对象所属组织
     */
    private String monitorGroupId;

    /**
     * 对象名称
     */
    private String monitorName;

    /**
     * 生成状态: 0: 未生成; 1:生成成功; 2:生成失败
     */
    private Integer status;

    /**
     * 所属组织
     */
    private String groupName;

    /**
     * 定位终端号
     */
    private String deviceNumber;

    /**
     * 对讲终端号
     */
    private String intercomDeviceId;

    /**
     * 原始机型
     */
    private String originalModelName;

    /**
     * 原始机型前5位
     */
    private String modelId;

    /**
     * 对讲机型名称
     */
    private String intercomModelName;

    /**
     * SIM卡
     */
    private String simcardNumber;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 个呼号码
     */
    private String number;

    /**
     * 组数
     */
    private Integer maxGroupNum;

    /**
     * 当前所在组数量
     */
    private Integer currentGroupNum;

    /**
     * 是否录音: 0: 不录音; 1: 录音
     */
    private Integer recordEnable;

    /**
     * 文本信息: 1:支持 ;0:不支持
     */
    private Integer textEnable;

    /**
     * 是否支持图片消息 1:支持 0:不支持
     */
    private Integer imageEnable;

    /**
     * 是否支持离线语音消息 1:支持 0:不支持
     */
    private Integer audioEnable;

    /**
     * 对讲终端密码
     */
    private String devicePassword;

    private String assignmentId;
    private String assignmentName;

    /**
     * 最大好友数
     */
    private Integer maxFriendNum;

    /**
     * 已使用的旋钮"逗号分隔"
     */
    private String knobNos;

    /**
     * 原始机型中的旋钮数量
     */
    private Integer knobNum;

    /**
     * 客户ID
     */
    private String customerCode;

    /**
     * SIM卡授权码
     */
    private String authCode;
}
