package com.zw.lkyw.domain.sendMessageReport;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

/**
 * 下发消息详情
 * @author denghuabing on 2020/1/2 14:34
 */
@Data
public class SendMessageDetail {

    private int num;
    private String monitorId;

    @ExcelField(title = "车牌号")
    private String monitorName;

    /**
     * 标识颜色(车牌颜色)
     */
    @ExcelField(title = "车辆颜色")
    private String signColor;

    @ExcelField(title = "车辆类型")
    private String objectType;

    @ExcelField(title = "所属企业")
    private String groupName;

    /**
     * 消息内容
     */
    @ExcelField(title = "消息内容")
    private String msgContent;

    /**
     * 播放方式 0:终端TTS读播 1:终端显示器显示 2:广告屏显示  (包含多种播放方式，以逗号隔开)
     */
    @ExcelField(title = "播放方式")
    private String playType;

    /**
     * 下发方式  0:系统下发 1:人工下发
     */
    @ExcelField(title = "下发方式")
    private String sendType;

    /**
     * 下发人
     */
    @ExcelField(title = "下发人")
    private String sendUserName;

    /**
     * 下发状态 0:下发成功 1: 下发失败
     */
    @ExcelField(title = "下发状态")
    private String sendStatus;

    /**
     * 下发时间 时间戳,单位:毫秒
     */
    @ExcelField(title = "下发时间")
    private String sendTime;
}
