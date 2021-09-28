package com.zw.protocol.msg.t809.body.module;

import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;

/**
 * 川冀标处理结果上报
 */
@Data
public class AlarmProcessAck implements T809MsgBody {
    private Integer supervisionId;

    /**
     * 报警处理结果
     * 0x00:处理中
     * 0x01:已处理完毕
     */
    private Integer result;

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

    /**
     * 桂标809需要的报警标识
     */
    private String alarmId;

    public static AlarmProcessAck getInstance(String company, String handleType, Integer supervisionId,
        String operator) {
        return getInstance(company, handleType, supervisionId, operator, null);

    }

    public static AlarmProcessAck getInstance(String company, String handleType, Integer supervisionId, String operator,
        String alarmId) {
        AlarmProcessAck alarmProcessAck = new AlarmProcessAck();
        alarmProcessAck.supervisionId = supervisionId;
        alarmProcessAck.result = getProcessResult(handleType);
        alarmProcessAck.method = getProcessMethod(handleType);
        alarmProcessAck.company = company;
        alarmProcessAck.companyLength = company.length();
        alarmProcessAck.operator = operator;
        alarmProcessAck.operatorLength = operator.length();
        alarmProcessAck.alarmId = alarmId;
        return alarmProcessAck;
    }

    /**
     * 川冀标协议查询处理结果
     * @param handleType
     * @return
     */
    private static Integer getProcessResult(String handleType) {
        switch (handleType) {
            case "监听":
            case "拍照":
            case "下发短信":
            case "不做处理":
            case "人工确认报警":
                return 1;
            default:
                return 0;
        }
    }

    /**
     * 川冀标协议查询报警处理方式
     * @param handleType
     * @return
     */
    private static Integer getProcessMethod(String handleType) {
        switch (handleType) {

            case "拍照":
                return 0x00;
            case "下发短信":
                return 0x01;
            default:
                return 0x03;
        }
    }
}
