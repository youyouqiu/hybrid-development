package com.zw.ws.entity.t808.video;

import lombok.Data;

import java.io.Serializable;

import com.alibaba.fastjson.JSONArray;

/**
 * 音视频通道参数下发
 * @author: lifudong
 */
@Data
public class ChannelParam {
	private Integer audioSum; //音频通道总数
	private Integer videoSum; //视频通道总数
	private Integer audioVideoSum; //音视频通道总数
	private JSONArray contrasts; //音视频通道对照表
}
