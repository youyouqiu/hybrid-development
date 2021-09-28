package com.zw.protocol.msg.rtp;

import com.zw.protocol.msg.VideoMsgBody;
import lombok.Data;

/**
 * 视频播放相关消息
 * @author penghj
 * @version 1.0
 * @date 2020/11/11 16:39
 */
@Data
public class VideoPlayMsg implements VideoMsgBody {
    /**
     * 播放类型(0:实时、1:回放、2:对讲)
     */
    private String playType;
    /**
     * 客户端类型（1:WEB浏览器；2:手机APP）
     */
    private Integer targetType;
    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     * 终端手机卡号
     */
    private String simcardNumber;
    /**
     * 终端通道号
     */
    private Integer channelNumber;

    /**
     *  终端发送到服务器数据的字节数
     */
    private Integer sendBytes;

    /**
     * 服务器发送到终端数据的字节数
     */
    private Integer recvBytes;

    /**
     * 开始连接时间
     */
    private String startTime;
    /**
     * 断开连接时间
     */
    private String endTime;
    /**
     * 唯一标识
     */
    private String uuid;
    /**
     * 用户ID
     */
    private String userID;
}
