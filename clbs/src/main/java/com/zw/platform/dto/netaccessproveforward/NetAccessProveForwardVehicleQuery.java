package com.zw.platform.dto.netaccessproveforward;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/7/20 14:30
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NetAccessProveForwardVehicleQuery extends BaseQueryBean {
    private static final long serialVersionUID = 4198201914834574640L;
    private String userId;
}
