package com.zw.platform.domain.reportManagement;

import lombok.Data;
import java.io.Serializable;

/**
 * 报警统计核查请求应答数据体（1406指令）——报警统计
 * @author XK
 */
@Data
public class WarnMsgStaticsInfo implements Serializable {
    /**
     * 报警类型
     */
    private Integer warnType;

    /**
     * 报警数量
     */
    private Integer statics;

}
