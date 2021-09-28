package com.zw.ws.entity.t808.video;

import lombok.Data;

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;

/**
 * 单独设置视频参数设置
 * @author: lifudong
 */
@Data
public class VideoPartParam {
	
	private Integer waySum; //需要单独设置视频参数的通道数量
	
    private JSONArray videoSettings; //单独设置视频参数设置列表

}
