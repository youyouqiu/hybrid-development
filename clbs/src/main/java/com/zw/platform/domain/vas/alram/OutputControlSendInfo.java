package com.zw.platform.domain.vas.alram;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

import java.util.List;

/***
 @Author lijie
 @Date 2020/5/28 9:48
 @Description 19版输出控制下发实体
 @version 1.0
 **/
@Data
public class OutputControlSendInfo implements T808MsgBody {

    /** 控制类型ID
     * 若为F3特殊标识，需要固定为F3 */
    private Integer type = 0xf3;

    /** 控制标志/控制参数 */
    private Integer sign;

    private Integer num;

    private List<OutputControlSend> infoList;
}
