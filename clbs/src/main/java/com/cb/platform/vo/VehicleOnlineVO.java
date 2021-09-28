package com.cb.platform.vo;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

/**
 * @author zhangsq
 * @date 2018/5/7 14:53
 */
@Data
public class VehicleOnlineVO {

    @ExcelField(title = "车牌号")
    private String vehicleBrandNumber;
    @ExcelField(title = "车牌颜色")
    private String vehicleBrandColor;
    @ExcelField(title = "车辆类型")
    private String vehicleType;
    @ExcelField(title = "所属道路运输企业")
    private String enterpriseName;
    @ExcelField(title = "在线时间段")
    private String timeSection;
    @ExcelField(title = "时间段定位条数")
    private Integer timeSectionNumber;

}
