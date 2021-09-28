package com.zw.platform.service.leaderboard;

import com.zw.platform.domain.leaderboard.MonitorEntity;

import java.util.List;


/***
 @Author gfw
 @Date 2018/10/22 15:57
 @Description 性能监控接口
 @version 1.0
 **/
public interface MonitorService {
    /**
     * 查询所有
     * @return
     */
    List<MonitorEntity> findAll();

    /**
     * 根据ip查询服务端信息
     * @param ip
     * @return
     */
    MonitorEntity findByIp(String ip);

    /**
     * 查询默认地址
     * @return
     */
    String findByDefault();
}
