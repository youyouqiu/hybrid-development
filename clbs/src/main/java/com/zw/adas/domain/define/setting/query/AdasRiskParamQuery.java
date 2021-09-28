package com.zw.adas.domain.define.setting.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询条件参数query
 * @author gfw
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdasRiskParamQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 分组id
     */
    private String assignmentId;
    /**
     * 组织id
     */
    private String groupId;

    /**
     * 协议类型
     */
    private Integer protocol;
    /**
     * 终端类别
     */
    private Integer terminalCategory;

    /**
     * 下发状态
     */
    private Integer sendStatus;

    /**
     * 状态信息 0:全部 1:在线 2:离线
     */
    private Integer statusInfo;
}
