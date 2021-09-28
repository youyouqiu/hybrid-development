package com.zw.platform.domain.leaderboard;

import lombok.Data;


/***
 @Author gfw
 @Date 2018/10/22 15:47
 @Description 性能监控表实体
 @version 1.0
 **/
@Data
public class MonitorEntity {
    /**
     * 服务器ip地址
     */
    private String ipAddress;
    /**
     * 服务器名称
     */
    private String serverName;
    /**
     * 在线状态 1:在线 0:下线
     */
    private Integer isOnline;
    /**
     * 服务器用途
     */
    private String serverWay;
    /**
     * 服务器系统名称
     */
    private String systemName;
}
