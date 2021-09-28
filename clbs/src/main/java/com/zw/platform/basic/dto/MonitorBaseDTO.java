package com.zw.platform.basic.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 监控对象基础信息DTO
 * @author zhangjuan
 * @date 2020/9/25
 */
@Data
public class MonitorBaseDTO {
    @ApiModelProperty(value = "监控对象ID")
    private String id;

    @ApiModelProperty(value = "监控对象名称 车：车牌号 人:人员编号 物：物品标号")
    private String name;

    @ApiModelProperty(value = "监控对象别名 车：别名 人:姓名  物:物品名称")
    private String alias;

    @ApiModelProperty(value = "监控对象所属组织的uuid")
    private String orgId;

    @ApiModelProperty(value = "监控对象所属组织名称")
    private String orgName;

    @ApiModelProperty(value = "监控对象类型 0:车 1：人 2:物")
    private String monitorType;

    @ApiModelProperty(value = "监控对象类型名称 车 人 物、导入时使用")
    private String monitorTypeName;

    @ApiModelProperty(value = "对象类型名称")
    private String objectTypeName;

    @ApiModelProperty(value = "是否绑定：0 未绑定 1绑定")
    private String bindType;

    @ApiModelProperty(value = "是否绑定对讲设备：0 未绑定 1绑定")
    private String intercomBindType;

    @ApiModelProperty(value = "详情")
    private String remark;
}
