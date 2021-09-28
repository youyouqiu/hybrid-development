package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.form.OBDVehicleTypeForm;
import com.zw.platform.domain.statistic.FaultCodeQuery;
import com.zw.platform.domain.statistic.form.FaultCodeForm;
import com.zw.platform.domain.statistic.info.FaultCodeInfo;
import com.zw.platform.util.common.BaseQueryBean;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface OBDVehicleTypeDao {

    Page<OBDVehicleTypeForm> findList(BaseQueryBean queryBean);

    boolean addVehicleType(OBDVehicleTypeForm form);

    OBDVehicleTypeForm findVehicleTypeById(String id);

    /**
     * 根据id批量获取obd车型
     * @param ids id
     * @return List<OBDVehicleTypeForm>
     */
    List<OBDVehicleTypeForm> getByIds(@Param("ids") Collection<String> ids);

    /**
     * 通过 车型名称/发动机类型 模糊搜索
     * @param fuzzyName 车型名称/发动机类型
     * @return List<OBDVehicleTypeForm>
     */
    List<OBDVehicleTypeForm> getByFuzzyName(String fuzzyName);

    boolean updateVehicleType(OBDVehicleTypeForm form);

    boolean delete(List<String> ids);

    /**
     * 查询是否绑定设置
     * @param id
     * @return
     */
    List<String> findOBDSetting(String id);

    List<OBDVehicleTypeForm> findExport(@Param("query") String query);

    boolean addByBatch(List<OBDVehicleTypeForm> list);

    List<OBDVehicleTypeForm> findByNameAndType(@Param("name") String name, @Param("type") Integer type);

    OBDVehicleTypeForm findById(String id);

    List<OBDVehicleTypeForm> findByCode(String code);

    /**
     * 故障码存储
     * @param faultCodeList faultCodeList
     * @return boolean
     */
    Boolean saveFaultCodes(@Param("faultCodeList") List<FaultCodeForm> faultCodeList);

    /**
     * 查询故障码
     * @param query query
     * @return list
     */
    Page<FaultCodeInfo> findFaultCodeInfoList(FaultCodeQuery query);

    /**
     * 车id查最新一条故障码信息
     * @param id
     * @return
     */
    FaultCodeInfo findFaultCodeByVid(String id);

    List<OBDVehicleTypeForm> findAll();

    /**
     * 获取车辆绑定的obd传感器信息
     * @param monitorId
     * @return
     */
    OBDVehicleTypeForm getObdSensorInfoByMonitorId(@Param("monitorId") String monitorId);

    /**
     * 获取车辆绑定的obd传感器信息
     * @param monitorIds 监控对象信息
     * @return obd传感器信息列表
     */
    List<OBDVehicleTypeForm> getObdSensorInfoByMonitorIds(@Param("monitorIds") Collection<String> monitorIds);

    /**
     * 获得绑定了obd传感器的车辆
     * @return
     */
    List<String> getBandObdSensorMonitorId();
}
