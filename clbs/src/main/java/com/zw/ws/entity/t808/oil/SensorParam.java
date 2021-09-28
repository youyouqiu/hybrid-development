/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.oil;

import lombok.Data;

import java.io.Serializable;

/**
 * 油位传感器标定参数项
 * <p>Title: SensorParam.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 *
 * @version 1.0
 * @author: Jiangxiaoqiang
 * @date 2016年9月18日下午5:02:36
 */
@Data
public class SensorParam implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 油位高度模拟量(单位：mm 毫米)
     */
    private double height;

    /**
     * 油量(单位：1/10L)
     */
    private double surplus;
}
