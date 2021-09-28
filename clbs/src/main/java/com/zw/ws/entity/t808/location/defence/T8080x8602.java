/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with ZhongWei.
 */

package com.zw.ws.entity.t808.location.defence;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

import java.util.List;

/**
 * <p>Title: T808_0x8602.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Jiangxiaoqiang
 * @date 2016年9月7日上午9:06:30
 * @version 1.0
 *
 */
@Data
public class T8080x8602 implements T808MsgBody {

    private static final long serialVersionUID = 1L;

    private Integer settingType;

    private Integer setParam;

    private Integer areaSum; // 包 区域数

    private List<RectangleAreaItem> areaParams;

}
