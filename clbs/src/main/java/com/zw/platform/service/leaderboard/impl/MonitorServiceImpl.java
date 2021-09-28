package com.zw.platform.service.leaderboard.impl;

import com.zw.platform.domain.leaderboard.MonitorEntity;
import com.zw.platform.repository.modules.MonitorDao;
import com.zw.platform.service.leaderboard.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/***
 @Author gfw
 @Date 2018/10/22 15:58
 @Description 性能监控接口实现
 @version 1.0
 **/
@Service("oldMonitorService")
public class MonitorServiceImpl implements MonitorService {

    @Autowired
    MonitorDao monitorDao;

    @Override
    public List<MonitorEntity> findAll() {
        return monitorDao.findAll();
    }

    @Override
    public MonitorEntity findByIp(String ip) {
        return monitorDao.findByIp(ip);
    }

    @Override
    public String findByDefault() {
        return monitorDao.findByDefault();
    }
}
