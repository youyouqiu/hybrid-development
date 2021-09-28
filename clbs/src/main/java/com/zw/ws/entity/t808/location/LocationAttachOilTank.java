/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * TODO  油位传感器详细数据
 * <p>Title: LocationAttachOilTank.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年11月3日下午5:41:10
 * @version 1.0
 */
@Data
public class LocationAttachOilTank implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private Integer len;

	/**
	 * 液位高度AD值
	 */
	private String ADHeight;

	/**
	 * 燃油温度
	 */
	private String oilTem;
	
	/**
	 * 环境温度
	 */
	private String envTem;
	
	/**
	 *  加油量
	 */
	private String add;
	
	/**
	 *  漏油量
	 */
	private String del;

	/**
	 *  油箱油量
	 */
	private String oilMass;
	
	/**
	 *  液位百分比
	 */
	private String percentage;

	/**
	 * 油位高度
	 */
	private String oilHeight;
}
