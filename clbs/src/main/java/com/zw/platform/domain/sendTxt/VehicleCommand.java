package com.zw.platform.domain.sendTxt;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

import java.util.List;

/**
 * 车辆控制
 * @author  Tdz
 * @create 2017-04-21 14:56
 **/
@Data
public class VehicleCommand implements T808MsgBody {

    /** 控制类型ID
     * 若为F3特殊标识，需要固定为F3 */
    private Integer type = 0xf3;

    private Integer sign;
    /**
     * 控制数量
     */
    private Integer num;

    private List<VehicleControllerInfo> infoList;
}
