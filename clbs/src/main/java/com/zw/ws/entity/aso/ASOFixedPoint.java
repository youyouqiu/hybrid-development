package com.zw.ws.entity.aso;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * <p>
 * Title:0X0219定点、 定位模式和 持续运行时间(持续运行时间暂不支持)
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年07月28日 13:56
 */
@Data
public class ASOFixedPoint implements T808MsgBody {
    private String locationTime;//定点时间
    private int type = 0X0219;//类型

}
