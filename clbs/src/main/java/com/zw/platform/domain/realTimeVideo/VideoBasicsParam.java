package com.zw.platform.domain.realTimeVideo;

import java.io.Serializable;

import lombok.Data;

/**
 * 视频操作下发基础参数
 * @author hujun
 * @version 创建时间：2017年12月28日 下午5:25:40
 */
@Data
public class VideoBasicsParam implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String vehicleId;//车辆id
    private Integer channelNum;//逻辑通道号
}
