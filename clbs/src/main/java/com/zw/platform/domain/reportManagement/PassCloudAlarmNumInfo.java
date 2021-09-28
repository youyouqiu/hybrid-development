package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/1/10 15:36
 */
@Data
public class PassCloudAlarmNumInfo implements Serializable {
    private static final long serialVersionUID = -7647880583897931618L;
    /**
     * 报警类型
     */
    private Integer alarmType;
    /**
     * 报警数量
     */
    private Integer totalNum;
    /**
     * 已处理的数量
     */
    private Integer processedNum;
}
