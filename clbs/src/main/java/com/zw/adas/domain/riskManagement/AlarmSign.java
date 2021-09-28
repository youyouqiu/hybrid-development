package com.zw.adas.domain.riskManagement;

import lombok.Data;

@Data
public class AlarmSign {
    /**
     * 报警编号
     */
    private String alarmId;

    /**
     * 终端编号
     */
    private String deviceNumber;
    /**
     * 终端手机号 / 终端ID
     */
    private String id;

    /**
     * 时间:YYMMDDhhmmss
     */
    private String time;

    /**
     * 序号 /
     */
    private Integer serialNumber;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 保留
     */
    private Integer reserve = 0;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 附件类型
     */
    private Integer mediaType;

    /**
     * 附件数据长度
     */
    private Integer fileDataLen;

    /**
     * 数据偏移量
     */
    private Integer offset = 0;

}
