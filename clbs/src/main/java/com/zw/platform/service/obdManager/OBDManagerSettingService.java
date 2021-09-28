package com.zw.platform.service.obdManager;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.form.OBDManagerSettingForm;
import com.zw.platform.domain.basicinfo.query.OBDManagerSettingQuery;
import com.zw.platform.domain.statistic.info.FaultCodeInfo;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

public interface OBDManagerSettingService {

    Page<OBDManagerSettingForm> findList(OBDManagerSettingQuery obdManagerSettingQuery);

    OBDManagerSettingForm findByVid(String vehicleId);

    JsonResultBean addObdManagerSetting(OBDManagerSettingForm form);

    /**
     * id查询
     */
    OBDManagerSettingForm findObdSettingById(String id);

    /**
     * 车id查询
     */
    List<OBDManagerSettingForm> findObdSettingByVid(String vid);

    /**
     * 获取参考对象信息
     */
    List<OBDManagerSettingForm> getReferentInfo(String vid, List<Integer> protocols);

    JsonResultBean updateObdManagerSetting(OBDManagerSettingForm form);

    JsonResultBean deleteObdManagerSetting(String id);

    void sendObdParam(List<JSONObject> list);

    JsonResultBean sendObdInfo(String vid, Integer commandType);

    /**
     * 根据车id查最新的故障码信息
     */
    FaultCodeInfo findFaultCodeByVid(String vid);

    /**
     * 判断监控对象是否绑定了OBD传感器
     */
    boolean findIsBandObdSensor(String monitorId);

    /**
     * 获取最后一条OBD数据
     * @param vehicleId vehicleId
     * @return JsonResultBean
     */
    JsonResultBean getCacheObd(String vehicleId);
}
