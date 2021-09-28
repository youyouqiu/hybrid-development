/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.ws.entity.common;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: ClientWebSocketRequest.java
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
 * @author Jiangxiaoqiang
 * @version 1.0
 * @since 2016年8月26日下午4:24:26
 */
@Data
public class ClientWebSocketRequest<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private ClientRequestDescription desc;

    private T data;
}
