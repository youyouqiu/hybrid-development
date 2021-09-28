package com.zw.platform.service.sensor;

import com.github.pagehelper.Page;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.domain.param.RemoteUpgradeSensorBasicInfo;
import com.zw.platform.domain.vas.sensorUpgrade.MonitorSensorUpgrade;
import com.zw.platform.domain.vas.sensorUpgrade.SenosrMonitorQuery;
import com.zw.platform.domain.vas.sensorUpgrade.SensorType;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author zhouzongbo on 2019/1/18 11:09
 */
public interface SensorUpgradeService extends IpAddressService {

    /**
     * 获取传感器列表
     */
    List<SensorType> getSensorIdList();

    /**
     * 分页查询绑定传感器的监控对象信息
     */
    Page<MonitorSensorUpgrade> findMonitorByPage(SenosrMonitorQuery query);

    /**
     * 批量升级
     * @param monitorIds   monitorIds
     * @param peripheralId 外设ID
     * @param file         file
     * @return JsonResultBean
     * @throws IOException
     */
    JsonResultBean sendRemoteUpgrade(String monitorIds, Integer peripheralId, MultipartFile file);

    /**
     * 中止远程升级
     * @param deviceId deviceId
     * @return JsonResultBean
     */
    JsonResultBean updateTerminationUpgrade(String deviceId);

    /**
     * 获取传感器基础参数
     * @param monitorId
     * @param sensorId
     * @return
     */
    RemoteUpgradeSensorBasicInfo getBasicInfo(String monitorId, String sensorId);
}
