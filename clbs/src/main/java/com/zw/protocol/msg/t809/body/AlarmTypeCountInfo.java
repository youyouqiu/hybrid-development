package com.zw.protocol.msg.t809.body;

import com.zw.platform.domain.reportManagement.WarnMsgStaticsInfo;
import com.zw.protocol.msg.MsgDesc;
import lombok.Data;

import java.util.List;

/**
 * 1406指令返回消息数据体
 * @author Xk
 */
@Data
public class AlarmTypeCountInfo {
    protected List<WarnMsgStaticsInfo> list;

    /**
     * 对应报警统计核查请求消息源子业务类型标识,中位标准新增字段
     */
    private Integer sourceDataType;
    /**
     * 对应报警统计核查请求消息源报文序列号,中位标准新增字段
     */
    private Integer sourceMsgSn;

    /**
     * 组装 9406应答指令1406 返回数据
     * @param list 统计列表
     * @param desc f3返回描述信息
     * @return alarmTypeCountInfo
     */
    public static AlarmTypeCountInfo getInstance(List<WarnMsgStaticsInfo> list, MsgDesc desc, Integer sourceMsgSn) {
        AlarmTypeCountInfo alarmTypeCountInfo = new AlarmTypeCountInfo();
        if (desc != null) {
            alarmTypeCountInfo.sourceDataType = desc.getMsgID();
            alarmTypeCountInfo.sourceMsgSn = sourceMsgSn;
        }
        alarmTypeCountInfo.setList(list);
        return alarmTypeCountInfo;
    }

    public int getDataLength() {
        Integer length = 6 * list.size();
        if (sourceMsgSn != null) {
            length = length + 6;
        }
        return length;
    }
}
