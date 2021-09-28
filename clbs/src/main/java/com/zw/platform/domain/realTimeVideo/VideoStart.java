package com.zw.platform.domain.realTimeVideo;

import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class VideoStart implements T808MsgBody {
    private static final long serialVersionUID = 1L;

    /**
     服务器地址
      */
    private String serverIp;

    /**
     TCP端口
     */
    private Integer tcpPort;

    /**
     UDP端口
     */
    private Integer udpPort;

    /**
     通道号
     */
    private Integer channelNum;

    /**
     视频类型
     */
    private Integer videoType;

    /**
     码流类型
     */
    private Integer streamType;

    /**
     存储器类型
     */
    private Integer storageType;

    /**
     回放方式
     */
    private Integer remoteMode;

    /**
     快进快退倍数
     */
    private Integer forwardOrRewind;

    /**
      开始时间
     */
    private String startTime;

    /**
     结束时间
     */
    private String endTime;

    private String deviceType;

    public void setVideoType(Integer videoType) {
        if (ProtocolEnum.T808_2011_1078.getDeviceType().equals(deviceType) && videoType != null) {
            //1078报批稿要做转换处理
            switch (videoType) {
                case 2:
                    //视频
                    this.videoType = 0;
                    break;
                case 0:
                    //音视频混传
                    this.videoType = 2;
                    break;
                default:
                    this.videoType = videoType;
            }
        } else {
            this.videoType = videoType;
        }

    }

    public void setStreamType(Integer streamType) {
        if (ProtocolEnum.T808_2011_1078.getDeviceType().equals(deviceType) && streamType != null) {
            //1078报批稿要做转换处理
            switch (streamType) {
                case 2:
                    //字码流
                    this.streamType = 1;
                    break;
                default:
                    this.streamType = 0;
            }
        } else {
            this.streamType = streamType;
        }

    }
}
