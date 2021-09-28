package com.zw.lkyw.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/1/17 9:37
 */
@Data
public class AlarmNumberCount implements Serializable {
    private static final long serialVersionUID = -7749421837140248464L;
    /**
     * 报警类型
     */
    private Integer alarmType;
    /**
     * 报警总数量
     */
    private Integer totalNum;
    /**
     * 已处理的报警数量
     */
    private Integer processedNum;
}
