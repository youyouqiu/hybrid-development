package com.zw.protocol.msg.rtp;


import com.zw.protocol.msg.VideoMsgBody;
import lombok.Data;


/**
 * <p> Title: <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年12月21日 9:35
 */
@Data
public class RtpData implements VideoMsgBody {

    /**
     * 负载类型(PT)：7 比特，此域定义了负载的格式，由具体应用决定其解释，协议可以规定负载类型码和负载格式之间一个默认的匹配。其他的负载类型码可以通过非 RTP 方法动态定义。RTP发送端在任意给定时间发出一个单独的 RTP
     * 负载类型；此域不用来复用不同的媒体流。
     */
    private Integer loadType;

    private String simCardNumber;// 终端设备卡号

    private String vehicleId; // 车辆id

    private String deviceNumber; // 终端编号

    private Integer channelNumber;// 音视频逻辑通道号

    private String startTime;  // 开始时间

    private String endTime; // 结束时间

    private Integer endFlag; // 1表示结束，0表示未结束

    /**
     * 数据类型 0000:视频I帧 0001：视频P帧 0010：视频B帧 0011：音频帧 0100：透传数据
     */
    private Integer dataType;

    private Integer dataLength;// 数据体长度

    //private Integer flowSize;// 流量值

    private Integer use; // 流量值

    private byte[] data;// 数据体

    private Integer receiveSubSize; //接收数据包总数

    private Integer deviceSubSize; //设备上报数据包序号

    private Integer requestType; //分包处理标记

    private Integer offOn; //开启状态 0关闭 1开启

    private Long subscribeTime; //订阅时间

    private Double flowTotalSize; // 流量B转MB
}
