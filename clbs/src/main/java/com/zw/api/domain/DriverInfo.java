package com.zw.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;

@ApiModel("驾驶员信息")
public class DriverInfo {
    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("从业资格证号")
    private String idNumber;

    @ApiModelProperty("发证机关")
    private String agency;

    @ApiModelProperty("有效期")
    private LocalDate expireDate;

    //region getters & setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }
    //endregion
}
