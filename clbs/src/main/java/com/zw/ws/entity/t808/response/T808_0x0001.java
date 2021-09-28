/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.response;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: T808_0x0001.java
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
 * @date 2016年9月9日上午9:21:47
 * @version 1.0
 */
@Data
public class T808_0x0001 implements Serializable {

	private static final long serialVersionUID = 1L;

	/// <summary>
	/// 应答消息流水号
	/// </summary>
	private Integer msgSNACK;

	/// <summary>
	/// 应答消息ID
	/// </summary>
	private Integer answerIDACK;

	/// <summary>
	/// 应答结果，0：成功/确认；1：失败；2：消息有误；3：不支持
	/// </summary>
	private Integer result;
}
