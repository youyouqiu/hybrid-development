package com.zw.platform.domain.other.protocol.jl.dto;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 违规车辆上报记录 导出模板
 * @author zwkj 2020-06-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolateVehicleExportDTO {

    @ExcelField(title = "监控对象名称")
    private String monitorName;
    @ExcelField(title = "违规时间")
    private String violateTime;
    @ExcelField(title = "违规类型")
    private String type;
    @ExcelField(title = "车牌颜色色")
    private String plateColor;
    @ExcelField(title = "所属企业")
    private String groupName;
    @ExcelField(title = "上报时间")
    private String uploadTime;
    @ExcelField(title = "上传状态")
    private String uploadState;
    @ExcelField(title = "操作人")
    private String operator;
}