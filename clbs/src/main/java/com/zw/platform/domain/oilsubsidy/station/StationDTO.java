package com.zw.platform.domain.oilsubsidy.station;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * 公交线路站点DTO
 *
 * @author zhangjuan 2020-10-09
 */
@Data
public class StationDTO {
    @NotNull(message = "【id】不能为空！", groups = {ValidGroupUpdate.class})
    private String id;

    @NotNull(message = "【站点名称】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(min = 1, max = 30, message = "【站点名称】最大30个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String name;

    @NotNull(message = "【站点编号】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(min = 1, max = 20, message = "【站点编号】最大20个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String number;

    @NotNull(message = "【站点经度】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiModelProperty(value = "站点经度")
    private Double longitude;

    @NotNull(message = "【站点纬度】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private Double latitude;

    @Size(max = 100, message = "【站点描述】最大100个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiModelProperty(value = "站点描述")
    private String describe;
    @Size(max = 50, message = "【备注】最大50个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiModelProperty(value = "备注")
    private String remark;
}
