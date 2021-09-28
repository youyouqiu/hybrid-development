package com.zw.platform.basic.dto.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车辆查询
 * @author zhnagjuan
 * @date 2020/9/28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VehiclePageQuery extends BaseQueryBean {
    /**
     * 行驶证查询类型 0:全部; 1:即将到期; 2:已到期
     */
    private Integer drivingLicenseType = 0;

    /**
     * 运输证查询类型 0:全部; 1:即将到期; 2:已到期
     */
    private Integer roadTransportType = 0;

    /**
     * 车辆保养车型类型 0:全部; 2:已到期
     */
    private Integer maintenanceType = 0;

}
