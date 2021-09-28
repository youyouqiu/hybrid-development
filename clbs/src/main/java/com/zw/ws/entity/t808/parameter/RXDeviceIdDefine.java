/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.parameter;

/**
 * R:指代硬件厂家瑞明  X:指代硬件传感器厂家信为 此ID定义目前为瑞明和信为两个厂家自主定义 ,遇到厂家之间五花八门的ID时需要淡定
 * <p>
 * Title: RXDeviceIdDefine.java
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
 * @date 2016年9月19日下午1:40:39
 * @version 1.0
 */
public class RXDeviceIdDefine {
	
	/**
	 * 在油路中安装，分别通过进油管和回油管中流过的油量，反映该发动机实际消耗的油量
	 * 0xF3是锐明需要的  45 46是信为的传感器需要的
	 */
	public static final int RUIMING_XINWEI_OIL_CONSUME_SENSOR_ID = 0xF345;// 瑞明、信为油耗传感器ID

	/**
	 * 在油路中安装，分别通过进油管和回油管中流过的油量，反映该发动机实际消耗的油量
	 * 0xF3是锐明需要的  45 46是信为的传感器需要的
	 */
	public static final int RUIMING_XINWEI_DOUBLE_OIL_CONSUME_SENSOR_ID = 0xF346;// 瑞明、信为双油耗传感器ID
}
