package com.zw.platform.vo.monitor;

import lombok.Data;


/***
 @Author gfw
 @Date 2018/10/22 10:03
 @Description 进程信息
 @version 1.0
 **/
@Data
public class MonitorProcessEntity {
    /**
     * 进程id
     */
    private String id;
    /**
     * 进程名
     */
    private String processName;
    /**
     * 进程Cpu
     */
    private String processCpu;
    /**
     * 进程存储
     */
    private String processMem;
}
