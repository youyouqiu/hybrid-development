package com.zw.platform.domain.basicinfo;

import lombok.Data;

import java.util.UUID;


/**
 * 终端型号视频参数设置实体
 */
@Data
public class DeviceChannelSettingInfo {
    /**
     * 通道id
     */
    private String id = UUID.randomUUID().toString();

    /**
     * 物理逻辑通道号
     */
    private Integer physicsChannel;

    /**
     * 逻辑通道号
     */
    private Integer logicChannel;

    /**
     * 通道类型: 0:音视频;1:音频;2:视频
     */
    private Integer channelType;

    /**
     * 是否连接云台；通道类型为0和2时，此字段有效；0:否;1:是
     */
    private Integer connectionFlag;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 码流类型 0：主码流 1：子码流
     */
    private Integer streamType;
}
