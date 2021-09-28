package com.zw.adas.domain.equipmentrepair;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 完成设备维修请求实体
 *
 * @author zhangjuan
 */
@Data
public class FinishDeviceRepairDTO {
    /**
     * 主键(企业id_报修时间_故障类型_车辆id)
     */
    @NotNull(message = "【主键】不能为空")
    private String primaryKey;

    /**
     * 维修日期 yyyy-MM-dd
     */
    @NotNull(message = "【维修日期】 不能为空")
    private String repairDate;
    
    /**
     * 备注
     */
    private String remark;

}
