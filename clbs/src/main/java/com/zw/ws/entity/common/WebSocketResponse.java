/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.common;

import com.zw.ws.entity.t808.location.RecievedLocationMessageDescription;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: WebSocketResponse.java
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
 * @date 2016年8月17日下午3:52:21
 * @version 1.0
 * @param <E>
 */
@Data
public class WebSocketResponse<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private RecievedLocationMessageDescription desc;
	
	private T data;
}
