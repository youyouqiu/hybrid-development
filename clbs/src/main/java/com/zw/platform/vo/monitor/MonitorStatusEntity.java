package com.zw.platform.vo.monitor;

import lombok.Data;


/***
 @Author gfw
 @Date 2018/10/22 9:25
 @Description 服务器状态实体
 @version 1.0
 **/
@Data
public class MonitorStatusEntity {
    /**
     * cpu状态 0:正常 1:异常
     */
    private String cpuStatus;
    /**
     * 内存状态 0:正常 1:异常
     */
    private String memStatus;
    /**
     * 存储空间状态 0:正常 1:异常
     */
    private String diskStatus;

    /**
     * 网络状态 0:正常 1:异常
     */
    private String networkStatus;

    /**
     * 网络流入 in
     */
    private String networkInflow;

    /**
     * 网络流出 out
     */
    private String networkOutflow;
}
