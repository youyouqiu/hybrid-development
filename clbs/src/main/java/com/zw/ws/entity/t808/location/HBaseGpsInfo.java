/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: HBaseGpsInfo.java
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * 
 * @author: Jiangxiaoqiang
 * @date 2016年9月21日下午2:09:49
 * @version 1.0
 */
@Data
public class HBaseGpsInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private String vehicle_id;

	private String vtime;

	private String alarm;

	private String status;

	private String longtitude;

	private String latitude;

	private String height;

	private String speed;

	private String angle;

	private String temperture;

	/**
	 * 保留
	 */
	private String reserve;

	/**
	 * 里程
	 */
	private String gps_mile;

	private String device_id_one;// 设备ID

	private String device_id_two;

	private String message_length_one;// 消息长度

	private String message_length_two;

	private String total_oilwear_one;// 累计油耗

	private String total_oilwear_two;

	private String oiltank_temperature_one;// 邮箱温度

	private String oiltank_temperature_two;

	private String transient_oilwear_one;// 瞬时油耗

	private String transient_oilwear_two;

	private String total_time_one;// 累计时间

	private String total_time_two;
}
