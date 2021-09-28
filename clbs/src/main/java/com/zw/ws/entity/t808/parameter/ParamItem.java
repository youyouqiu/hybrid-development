/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.ws.entity.t808.parameter;

import lombok.Data;

import java.io.Serializable;

/**
 * 终端参数项数据格式
 * @version 1.0
 */
@Data
public class ParamItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer paramId;

    private Integer paramLength;

    private Object paramValue;
}
