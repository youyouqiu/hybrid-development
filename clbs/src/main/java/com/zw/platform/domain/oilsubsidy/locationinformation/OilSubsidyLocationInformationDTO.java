package com.zw.platform.domain.oilsubsidy.locationinformation;

import com.zw.platform.util.excel.annotation.ExcelField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author XK
 */
@Data
public class OilSubsidyLocationInformationDTO {

    @ApiModelProperty(value = "定位信息id")
    private String id;

    @ApiModelProperty(value = "转发平台对应的企业id")
    private String forwardOrgId;

    @ExcelField(title = "转发平台组织")
    private String forwardOrgName;

    @ExcelField(title = "开始时间")
    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ExcelField(title = "结束时间")
    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ExcelField(title = "油补平台收到的定位数据量")
    @ApiModelProperty(value = "油补平台收到的定位数据量")
    private Long locationNum;

}
