/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;

/**
 * 车辆 I/O 输入检测
 */
@Data
public class IoSignalMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer signal0;

	private Integer signal1;

	private Integer signal2;

	private Integer signal3;
}
