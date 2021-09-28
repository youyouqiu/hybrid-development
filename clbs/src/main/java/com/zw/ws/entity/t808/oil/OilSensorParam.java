/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.oil;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: OilSensorParam.java
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
 * @date 2016年9月18日下午5:09:30
 * @version 1.0
 */
@Data
public class OilSensorParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 外设ID
	 */
	private Integer paramItemId;

	/**
	 * 参数长度
	 */
	private Integer paramItemLength;

	/**
	 * 波特率
	 */
	private Integer baudRate;

	/**
	 * 奇偶校验,奇偶校验位：1-奇校验；2-偶校验；3-无校验（缺省值）；
	 */
	private Integer parity;

	/**
	 * 量程
	 */
	private Integer range;

	/**
	 * 补偿使能
	 */
	private Integer inertiaCompEn;

	/**
	 * 滤波系数
	 */
	private Integer smoothing;

	/**
	 * 自动上传时间
	 */
	private Integer autoInterval;

	/**
	 * 输出修正系数K
	 */
	private Integer outputCorrectionK;

	/**
	 * 输出修正常数B
	 */
	private Integer outputCorrectionB;

	/**
	 * 燃料选择
	 */
	private Integer oilType;

	/**
	 * 油耗测量方案
	 */
	private Integer measureFun;

	/**
	 * 油箱形状
	 */
	private Integer fuelTankType;

	/**
	 * 油箱尺寸
	 */
	private Integer tankSize1;

	private Integer tankSize2;

	private Integer tankSize3;

	/**
	 * 加油时间阀值
	 */
	private Integer maxAddTime;

	/**
	 * 加油量阈值
	 */
	private Integer maxAddOil;

	/**
	 * 漏油时间阀值
	 */
	private Integer maxDelTime;

	/**
	 * 漏油量阀值
	 */
	private Integer maxDelOil;
	
	/**
	 *  保留项1
	 */
	private byte[] reservedItem1 = new byte[12];
	
	/**
	 *  保留项2
	 */
	private byte[] reservedItem2 = new byte[2];
	
	/**
	 *  保留项3
	 */
	private byte[] reservedItem3 = new byte[2];
	
	/**
	 *  保留项4
	 */
	private byte[] reservedItem4 = new byte[10];
}
