package com.zw.talkback.domain.intercom.info;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

/**
 * 原始机型INFO
 */
@Data
public class OriginalModelInfo {

    private String id;
    /**
     * 对讲机型名称
     */
    @ExcelField(title = "对讲机型名称")
    private String intercomName;

    /**
     * 设备类型ID
     */
    @ExcelField(title = "原始机型")
    private String modelId;

    /**
     * 是否支持视频功能
     * 1:支持
     * 0:不支持
     */
    private Integer videoAbility;

    @ExcelField(title = "视频")
    private transient String videoAbilityStr;

    /**
     * 是否支持实时视频功能
     * 1:支持 0:不支持
     */

    private Integer videoFuncEnable;

    @ExcelField(title = "实时视频")
    private transient String videoFuncEnableStr;

    /**
     * 视频路数
     */
    @ExcelField(title = "视频路数")
    private Integer chanlsNum;

    /**
     * 硬件旋钮个数
     */
    @ExcelField(title = "硬件旋钮个数")
    private Integer knobNum;

    /**
     * 最大支持群组数
     */
    @ExcelField(title = "最大群组数")
    private Integer maxGroupNum;

    /**
     * 最大支持好友数
     */
    @ExcelField(title = "最大好友数")
    private Integer maxFriendNum;

    /**
     * 是否支持监听功能
     * 1:支持
     * 0:不支持
     */
    private Integer interceptEnable;
    @ExcelField(title = "监听")
    private transient String interceptEnableStr;

    /**
     * 是否支持创建临时组功能
     * 1:支持
     * 0:不支持
     */
    private Integer tempGroupEnable;
    @ExcelField(title = "临时组")
    private transient String tempGroupEnableStr;

    /**
     * 是否支持传感功能
     * 1:支持
     * 0:不支持
     */
    private Integer sensorAbility;
    @ExcelField(title = "传感器")
    private transient String sensorAbilityStr;

    /**
     * 是否支持视频电话功能
     * 1:支持 0:不支持
     */
    private Integer videoCallEnable;
    @ExcelField(title = "持视频电话")
    private transient String videoCallEnableStr;

    /**
     * 是否支持视频会议功能
     * 1:支持 0:不支持
     */
    private Integer videoConferenceEnable;
    @ExcelField(title = "视频会议")
    private transient String videoConferenceEnableStr;

    /**
     * 原始机型ID(设备类型ID)
     */
    private Long index;
    /**
     * 设备类型别名
     */
    private String modelName;

    /**
     * 设备类型
     * 0: 普通对讲设备
     * 1: 调度台
     * 2:多媒体客户端
     * 3:音视频的设备
     * 4: 视频设备
     */
    private Integer type;

    /**
     * 传感控制器支持个数
     */
    private Integer seneorCtlMx;

    /**
     * 是否支持发送IM图片消息
     * 1:支持 0:不支持
     */
    private Integer sendImageEnable;
    /**
     * 是否支持GIS功能
     * 1:支持
     * 0:不支持
     */
    private Integer gisAbility;

    /**
     * 最大支持监听组个数
     */
    private Integer interceptNum;

    /**
     * 485串口个数
     */
    private Integer serial485num;
    /**
     * 是否支持定位功能
     */
    private Integer supportLocate;

    /**
     * 是否支持语音会议
     * 1:支持 0:不支持
     */
    private Integer audioConferenceEnable;

    /**
     * 是否支持发送离线语音消息
     * 1:支持 0:不支持
     */
    private Integer sendAudioEnable;
    /**
     * 是否支持围栏功能
     * 1:支持 0:不支持
     */
    private Integer fenceEnable;

    /**
     * 是否支持语音对讲功能
     * 1:支持
     * 0:不支持
     */
    private Integer audioAbility;
    /**
     * 是否支持巡更
     * 1:支持
     * 0:不支持image
     */
    private Integer patrolEnable;
    /**
     * 是否支持发送IM文本消息
     * 1:支持 0:不支持
     */
    private Integer sendTextEnable;
    /**
     * 232串口个数
     */
    private Integer serial232num;
    /**
     * 备注
     */
    private String comments;

    public void initExportData() {
        videoAbilityStr = getSupportData(videoAbility);
        videoFuncEnableStr = getSupportData(videoFuncEnable);
        interceptEnableStr = getSupportData(videoFuncEnable);
        tempGroupEnableStr = getSupportData(tempGroupEnable);
        sensorAbilityStr = getSupportData(sensorAbility);
        videoCallEnableStr = getSupportData(videoCallEnable);
        videoConferenceEnableStr = getSupportData(videoConferenceEnable);
    }

    private String getSupportData(Integer supportFlag) {
        return supportFlag == 1 ? "支持" : "不支持";
    }

}
