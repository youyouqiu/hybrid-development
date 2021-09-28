package com.zw.platform.domain.realTimeVideo;

import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

/**
 * 实时音视频传输请求0x9101
 * @author hujun
 * @version 创建时间：2018年1月2日 下午4:36:29
 */
@Data
public class VideoRequest implements T808MsgBody {

    /**
     *  实时视频服务器地址
     */
    private String serverIp;
    /**
     * 实时视频TCP端口
     */
    private Integer tcpPort;
    /**
     * 实时视频UDP端口
     */
    private Integer udpPort;
    /**
     * 逻辑通道号
     */
    private Integer channelNum;
    /**
     * 数据类型（0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传）
     */
    private Integer type;
    /**
     * 码流类型（0：主码流，1：子码流）
     */
    private Integer streamType;
    private String deviceType;

    public void setType(Integer type) {
        if (ProtocolEnum.T808_2011_1078.getDeviceType().equals(deviceType) && type != null) {
            //1078报批稿要做转换处理
            switch (type) {
                case 1:
                    //视频
                    this.type = 0;
                    break;
                case 2:
                    //双向对讲
                    this.type = 1;
                    break;
                case 3:
                    //监听
                    this.type = 2;
                    break;
                case 4:
                    //中心广播
                    this.type = 3;
                    break;
                case 5:
                    //透传
                    this.type = 4;
                    break;
                default:
                    this.type = type;
            }
        } else {
            this.type = type;
        }
    }
}
