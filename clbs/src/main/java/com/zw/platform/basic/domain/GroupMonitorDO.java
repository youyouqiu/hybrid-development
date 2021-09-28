package com.zw.platform.basic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author wanxing
 * @Title: 分组-监控对象DO ,和表一一对应
 * @date 2020/11/317:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class GroupMonitorDO extends BaseDO {

    /**
     * 分组Id
     */
    private String groupId;
    /**
     * 监控对象Id
     */
    private String vehicleId;
    /**
     * 监控对象类型 0:车，1：人，3：物
     */
    private String monitorType;

    /**
     * 组旋钮位置编号
     */
    private Integer knobNo;

    public GroupMonitorDO(String monitorId, String monitorType, String groupId) {
        super();
        this.groupId = groupId;
        this.vehicleId = monitorId;
        this.monitorType = monitorType;
    }
}
