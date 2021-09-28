/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with ZhongWei.
 */

package com.zw.ws.entity.t808.vehicle;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>Title: AlarmParam.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Jiangxiaoqiang
 * @date 2016年8月18日上午11:18:52
 * @version 1.0
 *
 */
@Data
public class AlarmParam implements Serializable {


    private static final long serialVersionUID = 1L;

    private String alarmId;

    private Integer alarmTypeId;

    /*
     * 区域报警标记,alarmType=21 或 alarmType=22时有效
     * */
    private Integer areaAlarmFlag;

    /**
     * 位置类型，1：圆形；2：矩形；3：多边形；4路线
     */
    private Integer zoneType;

    private Integer zoneId;

    /**
     * 进出区域方向，0：进；1：出
     */
    private Integer zoneDirection;

}
