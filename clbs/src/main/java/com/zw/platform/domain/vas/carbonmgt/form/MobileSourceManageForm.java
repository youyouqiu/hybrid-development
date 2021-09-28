package com.zw.platform.domain.vas.carbonmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/21.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MobileSourceManageForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @ExcelField(title = "车牌号")
    private String brand;//车牌号

    @ExcelField(title = "车组")
    private String groupName;//车组

    @ExcelField(title = "车辆类型")
    private String vehicleType;//车辆类型

    @ExcelField(title = "燃油类型")
    private String fuelType;//燃油类型

    @ExcelField(title = "gps时间")
    private long vtime;// gps时间

    @ExcelField(title = "累计总里程")
    private String sumGpsMile;//累计总里程

    @ExcelField(title = "阶段里程")
    private String stageMileage;//阶段里程

    @ExcelField(title = "累计总油耗")
    private String sumTotalOilwearOne;//累计总油耗

    @ExcelField(title = "阶段里程")
    private String stageTotalOilwearOne;//阶段里程

    @ExcelField(title = "空调状态")
    private String airConditionStatus;//空调状态

    @ExcelField(title = "空调开启时长")
    private String airConditioningDuration;//空调开启时长

    private String totalMileage;//总里程

    private String speed;//平均速度

    private String totalFuelConsumption;//油耗

    private String bTotalFuelConsumption;//百公里油耗

    private String co2;//二氧化碳

    private String bco2;//百公里二氧化碳

    private String startTime;//开始时间

    private String endTime;//结束时间

}
