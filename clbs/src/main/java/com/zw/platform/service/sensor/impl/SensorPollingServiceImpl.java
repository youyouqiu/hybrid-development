package com.zw.platform.service.sensor.impl;


import com.google.common.collect.Maps;
import com.zw.platform.domain.vas.f3.SensorPolling;
import com.zw.platform.repository.vas.SensorPollingDao;
import com.zw.platform.service.sensor.SensorPollingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * 轮询数据
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月09日 14:20
 */
@Service
public class SensorPollingServiceImpl implements SensorPollingService {
    private static Logger log = LogManager.getLogger(SensorPollingServiceImpl.class);

    @Autowired
    private SensorPollingDao sensorPollingDao;

    @Override
    public List<SensorPolling> findByVehicleId(String vehicleId) {
        try {
            return this.sensorPollingDao.findByVehicleId(vehicleId);
        } catch (Exception e) {
            log.error("error" + e);
        }
        return null;
    }

    @Override
    public Map<String, String> findAllSensorPoll() {

        Map<String, String> map = Maps.newHashMap();
        List<Map<String, String>> list = sensorPollingDao.findAllSensorPoll();
        for (Map<String, String> m : list) {
            map.put(m.get("vehicle_id"), m.get("pollingName"));
        }
        return map;

    }

    @Override
    public List<SensorPolling> findByConfigid(String configid) {
        try {
            return this.sensorPollingDao.findByConfigid(configid);
        } catch (Exception e) {
            log.error("error" + e);
        }
        return null;
    }

    @Override
    public SensorPolling findByid(String id) {
        try {
            return this.sensorPollingDao.findByid(id);
        } catch (Exception e) {
            log.error("error" + e);
        }
        return null;
    }

    @Override
    public void addSensorPolling(SensorPolling sensorPolling) {
        try {
            this.sensorPollingDao.addSensorPolling(sensorPolling);
        } catch (Exception e) {
            log.error("error" + e);
        }
    }

    @Override
    public boolean addByBatch(List<SensorPolling> sensorPollings) {
        try {
            this.sensorPollingDao.addByBatch(sensorPollings);
            return true;
        } catch (Exception e) {
            log.error("error" + e);
        }
        return false;
    }

    @Override
    public void deleteByConfigId(String configId) {
        try {
            this.sensorPollingDao.deleteByConfigId(configId);
        } catch (Exception e) {
            log.error("error" + e);
        }
    }

    @Override
    public String findStatus(String vehicleId) throws Exception {
        return sensorPollingDao.findStatus(vehicleId);
    }
}
