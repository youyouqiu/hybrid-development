package com.zw.protocol.msg.t809.body;


import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;


/**
 * Created by LiaoYuecai on 2018/2/7.
 */
@Data
public class AgingPwdUpData implements T809MsgBody {
    private Integer dataType;
    private String platformId;
    private String authorizeCode1;
    private String authorizeCode2;
}
