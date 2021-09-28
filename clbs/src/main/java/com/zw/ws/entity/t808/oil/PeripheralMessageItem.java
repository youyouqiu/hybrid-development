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
import java.util.List;

/**
 * 外设消息项列表
 * <p>Title: PeripheralMessageItem.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Jiangxiaoqiang
 * @date 2016年9月18日下午4:59:59
 * @version 1.0
 *
 */
@Data
public class PeripheralMessageItem<T> implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 外设ID
     */
    private Integer sensorID;

    /**
     * 长度(经锐明开发人员证实，此处实际数据为标定组数)
     */
    private Integer sensorSum;

    /**
     * 参数项
     */
    private List<T> demarcates;
}
