package com.zw.platform.domain.basicinfo.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月10日 9:43
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SensorConfigQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 2184966150167514075L;

    private String groupId;

    private String assignmentId;

    /**
     * 协议类型
     */
    private Integer protocol;

    private String vehicleId;
}
