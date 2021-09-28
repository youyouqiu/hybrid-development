package com.zw.platform.domain.netty;


import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * <p>
 * Title:解绑设备车辆
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年09月19日 9:16
 */
@Data
public class DeviceUnbound implements T808MsgBody {
    String identification;//唯一标识
    String deviceId;//终端id
    String deviceType;//协议类型
}
