package com.zw.platform.domain.param;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * 0x8403
 * @author zhouzongbo on 2019/5/29 11:07
 */
@Data
public class InformationParamInfo implements T808MsgBody {

    private static final long serialVersionUID = 5758923636123707675L;
    /**
     * 信息类型
     */
    private Integer type;
    /**
     * 信息内容长度
     */
    private Integer len;
    /**
     * 信息内容
     */
    private String value;

    private String deviceNumber;

    private String deviceId;

    private String simcardNumber;

    private Integer sendFrequency;

    private String vehicleId;

    private String deviceType;
}
