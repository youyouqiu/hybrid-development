package com.zw.adas.domain.riskManagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdasPicPostprocessInfo implements Serializable {
    private static final long serialVersionUID = 3310015528039402227L;

    /**
     * 图片URL
     */
    private String url;

    /**
     * 位置
     */
    private String address;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 方向
     */
    private Double direction;

    /**
     * 终端型号
     */
    private String terminalType;

    /**
     * 终端号
     */
    private String deviceNumber;
}
