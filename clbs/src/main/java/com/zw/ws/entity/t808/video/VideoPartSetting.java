package com.zw.ws.entity.t808.video;

import lombok.Data;

import java.io.Serializable;

/**
 * 视频参数
 * @author: lifudong
 */
@Data
public class VideoPartSetting {

    private Integer logicChannel; //逻辑通道号，参照附属表
    
	private Integer realCodeSchema; //实时码流格式  0:OCR(固定码率) 1:VBR(可变码率) 2:ABR(平均码率)100~127:自定义
	
	private Integer realResolutionRatio; //实时流分辨率  0:QCIF 1:CIF 2:WCIF 3:D1 4:WD1 5:720P 6:1080P 100~127:自定义
	
	private Integer realKeyframeEvery; //实时流关键帧间隔（范围:1~1000帧）
	
	private Integer realFrameRate; //实时流目标帧率(范围1~120(帧/s))
	
	private Integer realCodeRate; //实时流目标码率；单位为千位每秒(kbps)
	
	private Integer saveCodeSchema; //存储流编码模式  0:OCR(固定码率) 1:VBR(可变码率) 2:ABR(平均码率)100~127:自定义
	
	private Integer saveResolutionRatio; //存储流分辨率  0:QCIF 1:CIF 2:WCIF 3:D1 4:WD1 5:720P 6:1080P 100~127:自定义
	
	private Integer saveKeyframeEvery; //存储流关键帧间隔（范围:1~1000帧）
	
	private Integer saveFrameRate; //存储流目标帧率(范围1~120(帧/s))
	
	private Integer saveCodeRate; //存储流目标码率；单位为千位每秒(kbps)
	
	/**
	 * OSD字幕叠加设置
	 * 按位设置:0表示不叠加 1表示叠加
     * Bit0:日期和时间
     * Bit1:车牌号码
     * Bit2:逻辑通道号
     * Bit3:经纬度
     * Bit4:行驶记录速度
     * Bit5:卫星定位速度
     * Bit6:连续驾驶时间
     * Bit7~Bit8:保留
     * Bit11~Bit15:自定义
	 */
	private Integer osd;

}
