package com.zw.platform.dto.ouputcontrol;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/8 11:52
 */
@Data
public class OutputControlDTO {
    /**
     * 监控对象id
     */
    private String moId;
    /**
     * 监控对象名称
     */
    private String moName;
    /**
     * 所属企业
     */
    private String orgName;
    /**
     * 对象类型
     */
    private String vehicleType;
    /**
     * 下发时间
     */
    private String downTimeStr;
    /**
     * 状态内容
     */
    private String statusStr;
}
