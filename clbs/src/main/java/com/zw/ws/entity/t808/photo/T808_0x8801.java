/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.photo;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: T808_0x8801.java
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
 * @date 2016年8月30日下午5:28:05
 * @version 1.0
 */
@Data
public class T808_0x8801 implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer channelId;

	private Integer photoCommand;

	private Integer storeFlag;

	private Integer resolution;

	private Integer quality;

	private Integer brightness;

	private Integer contrast;

	private Integer saturation;

	private Integer chroma;
}
