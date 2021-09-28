package com.zw.platform.domain.reportManagement.form;

import lombok.Data;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/11/16 9:46
 */
@Data
public class VehBasicDO {
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 企业id
     */
    private String orgId;
    /**
     * 车牌号
     */
    private String brand;
    /**
     * 车辆状态：0停用 1启用
     */
    private Integer isStart;

    /**
     * 是否停运
     * @return
     */
    public boolean isOutOfService() {
        return isStart != null && isStart.equals(0);
    }
}
