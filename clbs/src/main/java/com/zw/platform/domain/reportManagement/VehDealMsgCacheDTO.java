package com.zw.platform.domain.reportManagement;

import lombok.Data;

/**
 * @Author: zjc
 * @Description:车辆下发短信信息dto
 * @Date: create in 2020/11/18 9:21
 */
@Data
public class VehDealMsgCacheDTO {
    /**
     * 处理总条数
     */
    private int handleTotal;

    /**
     * 处理方式为下发短信条数
     */
    private int sendMsm;
}
