package com.zw.platform.domain.realTimeVideo;


import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = false)
public class VideoChannelSetting extends BaseFormBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String vehicleId; // 监控对象id

    private Integer physicsChannel; // 物理通道号

    private Integer logicChannel; // 逻辑通道号

    private Integer channelType; // 通道号类型

    private Integer connectionFlag; // 是否连接云台

    private Boolean panoramic; // 是否360全景

    private Integer streamType;//码流类型（0：主码流，1：子码流）

    private Integer sort; // 排序

    private String mobile; // sim卡号

}