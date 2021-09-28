package com.zw.protocol.msg.t809.body;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.t809.T809MsgBody;
import com.zw.protocol.msg.t809.body.module.AlarmProcessAck;
import lombok.Data;

@Data
public class SupervisionAlarmInfo implements T809MsgBody {
    protected Integer dataType;
    protected Integer dataLength;
    private JSONObject data;
    protected Integer vehicleColor;
    protected String vehicleNo;
    /**
     * 对应报警附件目录请求消息源子业务类型标识
     */
    private Integer sourceDataType;
    /**
     * 对应报警附件目录请求请求消息源报文序列号
     */
    private Integer sourceMsgSn;

    /**
     * 川标主动安全特有实体
     * @param brand
     * @param color
     * @param ack
     * @return
     */
    public static SupervisionAlarmInfo getInstance(String brand, Integer color, AlarmProcessAck ack) {
        // 消息体
        SupervisionAlarmInfo supervisionAlarmInfo = new SupervisionAlarmInfo();
        supervisionAlarmInfo.dataType = ConstantUtil.T809_UP_WARN_MSG_ADPT_TODO_INFO;
        // 后续数据长度(数据部分字段长度相加)
        supervisionAlarmInfo.dataLength = (36 + ack.getCompanyLength() + ack.getOperatorLength());
        supervisionAlarmInfo.vehicleNo = brand;
        supervisionAlarmInfo.vehicleColor = color;
        supervisionAlarmInfo.data = MsgUtil.objToJson(ack);
        return supervisionAlarmInfo;
    }
}
