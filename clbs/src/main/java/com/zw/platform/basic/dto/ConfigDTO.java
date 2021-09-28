package com.zw.platform.basic.dto;

import com.zw.platform.basic.constant.InputTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 信息配置录入信息
 * @author zhangjuan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ConfigDTO extends BindDTO {
    @ApiModelProperty(value = "录入方式")
    private InputTypeEnum inputType;
    /*******车辆录入特性字段*************/
    @ApiModelProperty(value = "所属省份ID")
    private String provinceId;

    @ApiModelProperty(value = "所属省份")
    private String province;

    @ApiModelProperty(value = "所属城市ID")
    private String cityId;

    @ApiModelProperty(value = "所属城市")
    private String city;

    @ApiModelProperty(value = "所属区县")
    private String county;

    @ApiModelProperty(value = "车辆类型ID")
    private String vehicleTypeId;

    @ApiParam(value = "运营类别ID")
    private String vehiclePurpose;

    /*******人员录入特性字段*************/
    @ApiParam(value = "身份证号")
    private String identity;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    private String email;
    /*******物品录入特性字段*************/
    @ApiModelProperty(value = "物品类型")
    private String thingType;

    @ApiModelProperty(value = "物品类别")
    private String thingCategory;

    @ApiModelProperty(value = "物品型号")
    private String thingModel;

    @ApiModelProperty(value = "制造商")
    private String thingManufacturer;
    /*****终端录入特性字段*************/
    @ApiModelProperty(value = "注册信息-制造商ID")
    private String manufacturerId;

    @ApiModelProperty(value = "注册信息-终端型号")
    private String deviceModelNumber;

    @ApiModelProperty(value = "终端是否启用")
    private Integer deviceIsStart;

    @ApiModelProperty(value = "终端安装时间")
    private String installTimeStr;

    /*******SIM卡录入特性字段******/
    @ApiModelProperty(value = "sim卡是否启用")
    private Integer simIsStart;

    @ApiModelProperty(value = "ICCID")
    private String iccid;

    @ApiModelProperty(value = "IMSI")
    private String imsi;

    @ApiModelProperty(value = "运营商")
    private String operator;

    @ApiModelProperty(value = "套餐流量(M)")
    private String simFlow;

    @ApiModelProperty(value = "到期时间")
    private Date simEndTime;

    @ApiModelProperty(value = "入网标识（0代表未入网，1带表入网）")
    private Integer accessNetwork;

    @ApiModelProperty(value = "从业人员编号，多个从业人员选择第一个")
    private String cardNumber;

}
