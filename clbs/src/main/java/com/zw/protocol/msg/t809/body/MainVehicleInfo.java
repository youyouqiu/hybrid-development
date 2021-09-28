package com.zw.protocol.msg.t809.body;

import com.alibaba.fastjson.JSONObject;
import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;

/**
 * 车辆交换信息实体
 * Created by LiaoYuecai on 2017/2/10.
 */
@Data
public class MainVehicleInfo implements T809MsgBody {
    private Integer sourceDataType;
    private Integer sourceMsgSn;
    private String vehicleNo;
    private Integer vehicleColor;
    private Integer dataType;
    private Integer dataLength;
    private JSONObject data;
    private String externalVehicleId;
}
