package com.zw.ws.entity.aso;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * <p>
 * Title:透传命令 0X020A
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年07月28日 11:58
 */
@Data
public class ASOTransparent implements T808MsgBody {
    String content;//透传内容
    int type = 0X020A;//
}
