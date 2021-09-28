/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.location.defence;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Title: T808_0x8607.java
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
 * @date 2016年9月6日下午3:03:32
 * @version 1.0
 */
@Data
public class T808_0x8607  implements T808MsgBody {

	private static final long serialVersionUID = 1L;

	private Integer areaSum;

	private List<String> areaIDs = new ArrayList<String>();
}
