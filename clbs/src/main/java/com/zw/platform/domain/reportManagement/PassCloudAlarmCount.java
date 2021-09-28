package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/1/10 15:36
 */
@Data
public class PassCloudAlarmCount implements Serializable {
    private static final long serialVersionUID = 291935565435019651L;

    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 监控对象名称
     */
    private String monitorName;
    /**
     * 报警类型对应的数量和已处理的数量
     */
    List<PassCloudAlarmNumInfo> numInfo;
}
