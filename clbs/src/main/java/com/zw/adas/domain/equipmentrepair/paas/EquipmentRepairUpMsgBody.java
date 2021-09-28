package com.zw.adas.domain.equipmentrepair.paas;

import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;

/**
 * 设备维修上报上级平台消息体
 *
 * @author zhangjuan
 */
@Data
public class EquipmentRepairUpMsgBody implements T809MsgBody {
    private Integer sourceDataType;
    private Integer sourceMsgSn;
    private String vehicleNo;
    private Integer vehicleColor;
    private Integer dataType;
    private EquipmentRepairMsg data;
}
