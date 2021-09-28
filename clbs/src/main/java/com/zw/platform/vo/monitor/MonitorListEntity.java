package com.zw.platform.vo.monitor;

import lombok.Data;


/***
 @Author gfw
 @Date 2018/10/22 9:38
 @Description 服务器列表实体
 @version 1.0
 **/
@Data
public class MonitorListEntity {
    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * ip地址
     */
    private String ipAddress;

    /**
     * 是否在线
     */
    private String isOnline;

    /**
     * 操作系统
     */
    private String systemName;

    /**
     * 服务器用途
     */
    private String systemWay;

    /**
     * 运行时长
     */
    private String runTime;

    /**
     * 服务器状态
     */
    private String SystemStatus;
    /**
     * cpu状态 0:正常 1:异常 2:异常
     */
    private String cpuStatus;
    /**
     * 内存状态 0:正常 1:异常 2:异常
     */
    private String memStatus;
    /**
     * 存储空间状态 0:正常 1:异常 2:异常
     */
    private String diskStatus;

    /**
     * 网络状态 0:正常 1:异常 2:异常
     */
    private String networkStatus;
}
