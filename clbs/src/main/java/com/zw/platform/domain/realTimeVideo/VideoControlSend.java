package com.zw.platform.domain.realTimeVideo;

import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;


/**
 * 0x9102(音视频实时传输控制)消息ID实体,用于下发
 */
@Data
public class VideoControlSend implements T808MsgBody {

    /**
     * 逻辑通道号
     */
    private Integer channelNum;

    /**
     * 视频操作下发（打开视频、关闭视频、清除视频、暂停视频、恢复视频、主子码流切换、静音）
     * 控制指令
     */
    private Integer control;

    /**
     * 关闭音视频类型
     */
    public Integer closeVideoType = 0;

    /**
     * 切换码流类型
     */
    public Integer changeStreamType = 0;

    public String deviceType;

    public void setControl(Integer control) {
        if (ProtocolEnum.T808_2011_1078.getDeviceType().equals(deviceType)) {
            this.control = null;
            this.closeVideoType = null;
            this.changeStreamType = null;
        } else {
            this.control = control;
        }
    }
}
