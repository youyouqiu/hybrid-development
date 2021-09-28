package com.zw.platform.vo.monitor;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/***
 @Author gfw
 @Date 2018/10/22 9:48
 @Description 性能详情实体
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class MonitorDetailEntity extends MonitorListEntity {
    /**
     * cpu百分比
     */
    private String cpuPercent;
    /**
     * 内存百分比
     */
    private String memPercent;

    /**
     * 已使用内存
     */
    private String memUse;
    /**
     * 总内存
     */
    private String memTotal;
    /**
     * 硬盘百分比
     */
    private String diskPercent;
    /**
     * 网络流入 in
     */
    private String networkInflow;

    /**
     * 网络流出 out
     */
    private String networkOutflow;
    private List<MonitorProcessEntity> list;
}
