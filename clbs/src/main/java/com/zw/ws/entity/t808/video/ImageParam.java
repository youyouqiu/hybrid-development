package com.zw.ws.entity.t808.video;

import lombok.Data;

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;

/**
 * 图像分析参数下发
 * @author: lifudong
 */
@Data
public class ImageParam {
	/**
	 * 车辆核载人数
	 * 客运车辆核定载客人数，视频分析结果超过时产生报警
	 */
	private Integer loadPeople; 
	
	/**
	 * 疲劳驾驶阀值
	 * 视频分析疲劳报警驾驶阀值，超过时产生报警
	 */
	private Integer tiredThreshold;
	
}
