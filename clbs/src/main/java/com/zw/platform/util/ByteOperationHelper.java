/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.platform.util;

/**
 * <p>
 * Title: ByteOperationHelper.java
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
 * @date 2016年8月17日下午4:33:38
 * @version 1.0
 */
public class ByteOperationHelper {
	public static int parseVehicleStatus(int vehicleStatus, int offset) {
		int rs = (int)(vehicleStatus & (long)Math.pow(2, offset));
		return rs == 0 ? 0 : 1;
	}

	/**
	 * 解析所有状态信息（如：报警状态、车辆状态等）
	 * 
	 * @Title: parseByteOfAllType
	 * @param inputParseContent
	 * @param offset
	 * @return
	 * @return int
	 * @throws @author
	 *             Jiangxiaoqiang
	 */
	public static int parseByteOfAllType(int inputParseContent, int offset) {
		int rs = (int)(inputParseContent & (long)Math.pow(2, offset));
		return rs == 0 ? 0 : 1;
	}
}
