package com.zw.platform.domain.energy;


import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import java.io.Serializable;


@Data
public class IdleStandard extends BaseFormBean implements Serializable {

    private String vehicleId;// 车辆id

    private String brand;//车牌号

    private String groupName;// 所属企业名字

    private String vehicleType;// 车辆类型

    private String fuelOilType;// 燃油类型

    private String startTime;// 基准开始时间

    private String endTime;// 基准结束时间

    private long idleTime;// 怠速时长

    private Double idleMile;// 慢速里程

    private Double idleFuel;// 怠速油耗

    private long conditioningOpenTime;// 空调开启时间

    private Double idleBase;// 怠速油耗基准

    private Double idleBaseCap;// 怠速CO2排放基准

}
