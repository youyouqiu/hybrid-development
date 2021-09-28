/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.ws.entity.t808.location;


import lombok.Data;

import java.io.Serializable;


/**
 * <p>
 * Title: KafkaRecievedMessageDescription.java
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
 * @date 2016年8月10日下午2:53:39
 * @version 1.0
 */
@Data
public class RecievedLocationMessageDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sysTime;

    private String deviceNo;

    private Integer msgID;

    private String type;

    private String dId;

    private String ipAddress;

    private String deviceId;

    /**
     * 消息唯一ID
     */
    private String uuid;

}
