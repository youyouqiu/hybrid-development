package com.zw.platform.domain.param;

import lombok.Data;

import java.io.Serializable;

/**
 * 传感器基础参数（远程升级界面读取基本信息返回实体）
 * @author hujun
 * @date 2019/2/16 11:36
 */
@Data
public class RemoteUpgradeSensorBasicInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String monitorId;// 监控对象id

    private String monitorName;// 监控对象名称

    private String sensorNumber;// 传感器编号

    private String sensorOutId; // 外设ID
}
