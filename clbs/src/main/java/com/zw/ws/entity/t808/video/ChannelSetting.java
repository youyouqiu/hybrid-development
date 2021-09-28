package com.zw.ws.entity.t808.video;

import lombok.Data;

/**
 * 音视频通道列表设置参数
 * @author: lifudong
 */
@Data
public class ChannelSetting {
    /**
	 * 物理通道号
	 */
    private Integer physicsChannel;
    /**
	 * 逻辑通道号
	 */
    private Integer logicChannel;
    /**
	 * 通道类型
	 * 0:音视频
	 * 1:音频
	 * 2:视频
	 */
    private Integer channelType;
    /**
	 * 是否连接云台；通道类型为0和2时，辞字段有效
	 * 0:未连接
	 * 1:连接
	 */
    private Integer connectionFlag;
}
