/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.common;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: ClientWebSocketRequestHead.java
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
 * @date 2016年8月29日上午9:38:44
 * @version 1.0
 */
@Data
public class ClientWebSocketRequestHead implements Serializable {

	private static final long serialVersionUID = 1L;

	private ClientRequestDescription desc;
}
