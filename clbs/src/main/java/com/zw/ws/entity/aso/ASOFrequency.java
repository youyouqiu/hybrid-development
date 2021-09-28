package com.zw.ws.entity.aso;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * <p>
 * Title:0X0218 上传频率
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年07月28日 11:59
 */
@Data
public class ASOFrequency implements T808MsgBody {

    public Integer frequencyTime;//上传频率时间 秒
    public Integer locationType;//定位模式
    private int type = 0X0218;//类型

}
