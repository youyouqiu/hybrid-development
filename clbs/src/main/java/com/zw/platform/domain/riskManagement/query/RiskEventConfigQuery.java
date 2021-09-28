package com.zw.platform.domain.riskManagement.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class RiskEventConfigQuery extends BaseQueryBean implements Serializable {
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
