package com.zw.protocol.msg.t809.body;

import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wanxing
 * @Title: 油补上报
 * @date 2020/10/1310:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OilSubsidyUpData implements T809MsgBody {

    private String companyId;
    private Integer dataType;
    private Object data;
    private Integer dataLength;
}
