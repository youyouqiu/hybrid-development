package com.zw.platform.vo.monitor;

import lombok.Data;

import java.io.Serializable;


/***
 @Author gfw
 @Date 2018/9/25 20:08
 @Description redis性能参数实体
 @version 1.0
 **/
@Data
public class MonitorRedisEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 系统启动时间
     */
    private String systemStartTime;

    /**
     * 系统运行时间
     */
    private String systemRunningTime;

    /**
     * 服务器ip地址
     */
    private String ipAddress;

    /**
     * 用户占用总的CPU
     */
    private String systemCpu;
    /**
     * 服务器总共内存
     */
    private String systemMemTotal;
    /**
     * 服务器使用内存
     */
    private String systemMemUse;
    /**
     * 服务器使用硬盘
     */
    private String systemDisk;
    /**
     * 服务器进程数组
     */
    private String processInfo;
    /**
     * 服务器网络流入
     */
    private String networkIn;
    /**
     * 服务器网络流出
     */
    private String networkOut;
    /**
     * 服务器每个 CPU 核心数
     */
    private String coreNum;

    /**
     * 服务器cpu 个数
     */
    private String cpuNum;
}
