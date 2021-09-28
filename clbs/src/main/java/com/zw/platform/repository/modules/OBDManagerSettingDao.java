package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.form.OBDManagerSettingForm;
import com.zw.platform.domain.basicinfo.query.OBDManagerSettingQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface OBDManagerSettingDao {

    List<OBDManagerSettingForm> findList(List<String> ids);

    /**
     * 查询监控对象obd设置
     * @param moIds 监控对象id
     * @return List<OBDManagerSettingForm>
     */
    List<OBDManagerSettingForm> getListByMoIds(@Param("moIds") Collection<String> moIds);

    /**
     * 查询监控对象obd设置
     * @param ids 设置id
     * @return List<OBDManagerSettingForm>
     */
    List<OBDManagerSettingForm> getListByIds(@Param("ids") Collection<String> ids);

    boolean addOBDManagerSetting(OBDManagerSettingForm form);

    List<OBDManagerSettingForm> getReferentInfo(@Param("userId") String userId, @Param("groupId") List<String> groupId,
        @Param("protocols") List<Integer> protocols);

    boolean updateOBDManagerSetting(OBDManagerSettingForm form);

    OBDManagerSettingForm findOBDSettingById(String id);

    List<OBDManagerSettingForm> findOBDSettingByVid(String vid);

    boolean deleteOBDManagerSetting(@Param("ids") Collection<String> ids);

    Page<OBDManagerSettingForm> findOBDSetting(@Param("query") OBDManagerSettingQuery query,
        @Param("userId") String userId, @Param("groupList") List<String> groupList);

    /**
     * 查找绑定过的OBD（初始化缓存使用）
     * @return
     */
    List<Map<String, String>> findAllSetting();

    /**
     * 查找监控对象是否绑定了OBD传感器
     * @param monitorId
     * @return
     */
    Integer findIsBandObdSensor(@Param("monitorId") String monitorId);

    /**
     * 根据obd车型id查询
     * @param obdVehicleTypeIds obd车型id
     * @return List<OBDManagerSettingForm>
     */
    List<OBDManagerSettingForm> getByObdVehicleTypeIds(
        @Param("obdVehicleTypeIds") Collection<String> obdVehicleTypeIds);
}
