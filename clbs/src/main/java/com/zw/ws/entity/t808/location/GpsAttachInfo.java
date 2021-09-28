/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */

package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: GpsAttachInfo.java
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
 * @date 2016年8月10日下午2:58:11
 * @version 1.0
 */
@Data
public class GpsAttachInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Double mileage;

	private Integer gpsAttachInfoLen;

	private Integer gpsAttachInfoID;

	private Integer oil;

	private Integer speed;
	
	private Integer signalState;

	private Integer analogQuantity;

	private Integer signalIntensity;

	private Integer GNSSNumber;

	private OilMessage oilMsg;

	private LineOutAlarm lineOutAlarm;

	private SpeedAlarm speedAlarm;

	private TimeOutAlarm timeOutAlarm;

}
