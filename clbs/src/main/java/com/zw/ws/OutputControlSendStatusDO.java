package com.zw.ws;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/8 9:42
 */
@Data
@NoArgsConstructor
public class OutputControlSendStatusDO {
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 下发状态：
     * 0:参数已生效; 1:参数未生效; 2:参数消息有误; 3:参数不支持;
     * 4:参数下发中; 5:终端离线，未下发; 7:终端处理中; 8:终端接收失败;
     */
    private Integer status;

    public OutputControlSendStatusDO(String monitorId, Integer status) {
        this.monitorId = monitorId;
        this.status = status;
    }
}
