package com.zw.api.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 程序名 : Rfid
 * 建立日期: 2020-10-20 9:06
 * 作者 : nixiangqian
 * <p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Rfid extends F3Sensor {

    @ApiModelProperty("RFID 卡数量")
    private Integer count;

    /**
     * RFID 卡详情数据
     */
    @ApiModelProperty("RFID 卡详情数据")
    private List<RfidCardInfo> cardInfos = new ArrayList<>();
}
