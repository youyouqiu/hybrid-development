package com.zw.platform.domain.realTimeVideo;

import java.util.Date;

import com.zw.platform.util.common.BaseFormBean;

import lombok.Builder;
import lombok.Data;

/**
 * @author Chen Feng
 * @version 1.0 2017/12/28
 */
@Data
@Builder
public class VideoTrafficInfo extends BaseFormBean {
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 通道号
     */
    private int channel;
    /**
     * 开始统计时间
     */
    private Date startTime;
    /**
     * 结束统计时间
     */
    private Date endTime;
    /**
     * 消耗流量值
     */
    private long flowValue;
    
    /**
     * 视频关闭flag
     */
    private int stopFlag;
}
