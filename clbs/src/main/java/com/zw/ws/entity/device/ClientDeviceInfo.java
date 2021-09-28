/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.device;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: ClientDeviceInfo.java
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
 * @date 2016年8月30日上午11:55:52
 * @version 1.0
 */
@Data
public class ClientDeviceInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String groupId;

	private String deviceId;

	private Integer channelCount;

	private String protocolType;
}
