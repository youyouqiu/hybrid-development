/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of 
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.text;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>Title: T808_0x8300.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Jiangxiaoqiang
 * @date 2016年8月31日上午9:01:45
 * @version 1.0
 * 
 */
@Data
public class T808_0x8300 implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Integer Flag;
	
	private String Text;	
}
