package com.zw.platform.domain.energy;


import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = true)
public class IdleEnergy extends BaseFormBean implements Serializable {
    private String vehicleId;// 车辆id

    private long idleTime;// 总怠速时长

    private Double idleTotal;// 总怠速油耗量

    private Double idleBaseList;// 怠速油耗基准(基准计算的)

    private Double idleMile;// 慢速里程

    private Double idleTotalCap;// 怠速CO2总排放量

    private Double idleBaseCap; // 怠速CO2排放基准
}
