package com.zw.adas.domain.riskManagement.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AdasRiskEventConfigQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /*
     * 车辆id
     */
    private String vehicleId;
    /*
     * 分组id
     */
    private String assignmentId;
    /*
     * 组织id
     */
    private String groupId;

}
