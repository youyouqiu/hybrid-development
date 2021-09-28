/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.location.defence;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: PolygonNodeItem.java
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
 * @date 2016年9月7日上午9:14:31
 * @version 1.0
 */
@Data
public class PolygonNodeItem implements Serializable {

	private static final long serialVersionUID = 1L;

	/// <summary>
	/// 顶点纬度
	/// </summary>
	private Double latitude;

	/// <summary>
	/// 顶点经度
	/// </summary>
	private Double longitude;
}
