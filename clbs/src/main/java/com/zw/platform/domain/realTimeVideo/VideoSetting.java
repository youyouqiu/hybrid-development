package com.zw.platform.domain.realTimeVideo;


import java.io.Serializable;

import com.zw.platform.util.common.BaseFormBean;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class VideoSetting extends BaseFormBean implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String vehicleId;  // 监控对象id

    private Integer allChannel;  // 全部逻辑通道号(为1则代表选中,为0则代表是选择的单独的通道号)

    private Integer logicChannel;  // 选中的逻辑通道号(当选择全部逻辑通道号时,该字段值为0)

    private Integer realCodeSchema; // 实时流编码模式

    private Integer realResolutionRatio; // 实时流分辨率

    private Integer realKeyframeEvery; // 实时流关键帧间隔

    private Integer realFrameRate; // 实时流目标帧率

    private Long realCodeRate; // 实时流目标码率

    private Integer saveCodeSchema; // 存储流编码模式

    private Integer saveResolutionRatio; // 存储流分辨率

    private Integer saveKeyframeEvery; // 存储流关键帧间隔

    private Integer saveFrameRate; // 存储流目标帧率

    private Long saveCodeRate; // 存储流目标码率

    /**
     * OSD字幕叠加设置(按位设置:0表示不叠加 1表示叠加):Bit0:日期和时间;Bit1:车牌号码;Bit2:逻辑通道号;Bit3:经纬度;Bit4:行驶记录速度
     ;Bit5:卫星定位速度;Bit6:连续驾驶时间
     */
    private Integer osd;

    private Integer audioSettings; // 是否启用音频输出

    /**
     * 车辆ID
     */
    private String videoSettingVid;

    /**
     * 通道号ID
     */
    private String videoChannelVid;

    private String videoSleepVid;

    private String videoRecordingVid;

}