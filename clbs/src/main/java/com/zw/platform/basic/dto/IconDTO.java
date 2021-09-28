package com.zw.platform.basic.dto;

import lombok.Data;

/**
 * 图标实体
 */
@Data
public class IconDTO {
    private String id;
    /**
     * 图标名称
     */
    private String icoName;

    /**
     * 图标地址
     */
    private String icoUrl;

    /**
     * 车辆图标系统默认为0，用户上传为1
     */
    private Integer defultState;

    /**
     * 监控对象类型
     */
    private String monitorType;
}
