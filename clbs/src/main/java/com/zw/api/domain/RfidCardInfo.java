package com.zw.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 程序名 : Rfid
 * 建立日期: 2020-10-20 9:06
 * 作者 : nixiangqian
 * <p>
 */
@Data
@ApiModel(description = "RFID 卡具体信息")
public class RfidCardInfo {

    @ApiModelProperty("卡编号")
    private String number;

    @ApiModelProperty("业务数据1")
    private Integer business1;

    @ApiModelProperty("业务数据2")
    private Integer business2;

    @ApiModelProperty("业务数据3")
    private Integer business3;


    @ApiModelProperty("业务数据4")
    private Integer business4;


}
