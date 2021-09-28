package com.zw.platform.repository.modules;

import com.zw.platform.domain.leaderboard.MonitorEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/***
 @Author gfw
 @Date 2018/10/22 15:46
 @Description 性能监控表
 @version 1.0
 **/
public interface MonitorDao {
    List<MonitorEntity> findAll();

    MonitorEntity findByIp(@Param(value = "ip") String ip);

    String findByDefault();
}
