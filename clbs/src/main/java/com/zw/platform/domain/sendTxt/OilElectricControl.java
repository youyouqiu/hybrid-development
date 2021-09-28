package com.zw.platform.domain.sendTxt;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

import java.util.List;

/**
 * 断油电控制
 * @author denghuabing
 * @version V1.0
 * @date 2021/1/25
 **/
@Data
public class OilElectricControl implements T808MsgBody {

    private Integer type = 0xf3;

    private Integer sign;
    /**
     * 控制数量
     */
    private Integer num;

    private List<OilElectric> infoList;
}
