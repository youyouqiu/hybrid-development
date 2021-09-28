package com.zw.platform.domain.realTimeVideo;

import lombok.Data;


/**
 * 视频窗口音视频传输控制指令参数实体
 */
@Data
public class AudioVideoRransmitForm {

    private String vehicleId; // 监控对象Id

    private String channelNum; // 通道号

    private int orderType;  // 指令类型(参照下发指令类型字典)

    private int control; // 控制指令（关闭视频、清除视频、暂停视频、恢复视频、主子码流切换、静音）

    private int closeVideoType; // 关闭音视频类型(音视频都关,关音频保留视频,关视频保留音频)

    private int changeStreamType; // 切换码流类型 0主码流 1子码流
    
    private String channelType; // 通道号类型
    
    private String userName;

    private Integer requestType; //请求方式
    
    private String unique; // 唯一标识

    private String equipmentType;// 设备类型（判断是否为手机设备）

    private String deviceType;
    //
    // public void setControl(int control) {
    //     if (ProtocolEnum.KKS_EV25.getDeviceType().equals(deviceType)) {
    //         //报批稿的设置需要
    //         switch (control) {
    //             case 2:
    //                 this.control = 0;
    //                 break;
    //             case 3:
    //                 this.control = 2;
    //                 break;
    //             default:
    //                 this.control = control;
    //         }
    //     } else {
    //         this.control = control;
    //     }
    //
    // }
}
