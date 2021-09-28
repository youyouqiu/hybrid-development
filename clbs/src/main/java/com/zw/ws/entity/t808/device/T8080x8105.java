/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary
 * information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you
 * entered into with ZhongWei.
 */

package com.zw.ws.entity.t808.device;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * 油量传感器标定
 * <p>
 * Title: T808_0x8900.java
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
 * @version 1.0
 * @author: Jiangxiaoqiang
 * @date 2016年9月18日下午4:56:33
 */
@Data
public class T8080x8105<T> implements T808MsgBody {

    private static final long serialVersionUID = 1L;
    private Integer cw;//命令字
    private String param;//命令参数
}
