package com.zw.protocol.msg;

import lombok.Data;
import lombok.EqualsAndHashCode;

/***
 @Author zhengjc
 @Date 2019/5/27 17:03
 @Description 协议下发实体调整，因为影响范围比较大所以采用继承
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class MsgDescExtend  extends MsgDesc {

    /**
     * 车牌颜色
     */
    private Integer plateColor;
}
