/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: RecievedLocationMessageHead.java
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
 * @date 2016年8月10日下午3:14:25
 * @version 1.0
 */
@Data
public class RecievedLocationMessageHead implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer subPackageNO;

	private Integer msgSN;//消息流水号Serial Number(1-65535)

	private Integer subPackages;

	private Integer encodeMod;

	private Integer subPackageSum;

	private Integer msgID;

	private String mobile;

	private Integer msgBodySize;

	private String head;
}
