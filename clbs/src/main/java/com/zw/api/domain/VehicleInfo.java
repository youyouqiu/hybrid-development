package com.zw.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.apache.bval.constraints.NotEmpty;

@ApiModel("新增车辆信息")
public class VehicleInfo {
    @NotEmpty
    @ApiModelProperty("车牌号")
    private String name;

    @NotEmpty
    @ApiModelProperty("车牌颜色，1:蓝色 2:黄色 3:黑色 4:白色 5:绿色 9:其它 90:农蓝 91:农黄 92:农绿 93:黄绿色 94:渐变绿色")
    private int plateColor;

    @NotEmpty
    @ApiModelProperty("终端手机号")
    private String simNo;

    @NotEmpty
    @ApiModelProperty("终端号")
    private String deviceNo;

    @NotEmpty
    @ApiModelProperty(
        "终端协议类型，1:JTT808-2013 11:JTT808-2019 12: 川标 13:冀标 14:桂标 15:苏标 16:浙标 17:吉标 18:陕标 19:赣标 20:沪标 24:京标 25:黑标")
    private String protocol;

    @NotEmpty
    @ApiModelProperty("组织机构代码")
    private String orgCode;

    //region Getter & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlateColor() {
        return plateColor;
    }

    public void setPlateColor(int plateColor) {
        this.plateColor = plateColor;
    }

    public String getSimNo() {
        return simNo;
    }

    public void setSimNo(String simNo) {
        this.simNo = simNo;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }
    //endregion
}
