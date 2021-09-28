package com.zw.protocol.msg.t809.body.module;

import com.zw.platform.domain.reportManagement.T809AlarmForwardInfoMiddleQuery;
import lombok.Data;

/**
 * 报警督办应答消息体
 */
@Data
public class PlatformAlarmAckInfo extends PlatformAlarmAck {

    /**
     * 报警处理方式
     * 0x00:快速拍照
     * 0x01:语音下发
     * 0x02:不做处理
     * 0x03:其他
     */
    private Integer method;

    /**
     * 报警处理人姓名长度
     */
    private Integer operatorLength;

    /**
     * 报警处理人姓名
     */
    private String operator;

    /**
     * 报警处理人所属公司名称长度
     */
    private Integer companyLength;

    /**
     * 报警处理人所属公司名称
     */
    private String company;

    public static PlatformAlarmAckInfo getInstance(String handleType, T809AlarmForwardInfoMiddleQuery alarmInfo) {
        PlatformAlarmAckInfo ack = new PlatformAlarmAckInfo();
        // 报警编号
        ack.setSupervisionId(alarmInfo.getMsgSn());
        ack.setResult(getResultFromHandleType(handleType));
        ack.setSourceMsgSn(alarmInfo.getMsgSn());
        ack.setSourceDataType(alarmInfo.getMsgId());
        ack.method = getProcessMethod(handleType);
        return ack;
    }

    public PlatformAlarmAckInfo assembleDealerInfo(String operator, String company) {
        this.company = company;
        this.companyLength = company.length();
        this.operator = operator;
        this.operatorLength = operator.length();
        return this;
    }

    /**
     * zw标准查询报警处理方式
     * @param handleType
     * @return
     */
    private static Integer getProcessMethod(String handleType) {
        switch (handleType) {

            case "拍照":
                return 0x00;
            case "下发短信":
                return 0x01;
            case "不做处理":
                return 0x02;
            default:
                return 0x03;
        }
    }

}
