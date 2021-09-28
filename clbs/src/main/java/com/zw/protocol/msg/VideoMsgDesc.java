package com.zw.protocol.msg;


import lombok.Data;


/**
 * Created by LiaoYuecai on 2017/6/19.
 */
@Data
public class VideoMsgDesc {
    private Integer msgId;// 消息号

    private String mobile;// SIM卡号

    private String deviceId;// 设备UUID

    private String deviceNumber;// 设备号

    private String brand;// 车牌号

    private String vehicleId;// 车辆UUID

    private Integer channelNumber;// 通道号

    private Integer resourceType;// 资源类型 0音视频/视频 1(对讲、监听、广播)

    private Integer requestType;// 请求方式 0实时视频 1资源回放
}
