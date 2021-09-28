package com.zw.platform.repository.vas;

import com.zw.platform.domain.vas.oilmgt.FluxSensor;
import com.zw.platform.domain.vas.oilmgt.form.FluxSensorForm;
import com.zw.platform.domain.vas.oilmgt.query.FluxSensorQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>Title: 流量传感器Dao</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016年9月19日上午9:16:03
 */
public interface FluxSensorDao {

    /**
     * 查询流量传感器
     */
    List<FluxSensor> findSensor(FluxSensorQuery query);

    /**
         * @param id
     * @return FluxSensor
     * @throws
     * @Title: 根据id查询
     * @author wangying
     */
    FluxSensor findById(String id);

    /**
         * @param number
     * @return FluxSensor
     * @throws
     * @Title: 根据number查询
     * @author wangying
     */
    FluxSensor findByNumber(String number);

    /**
         * @param form
     * @return boolean
     * @throws
     * @Title: 新增流量传感器
     * @author wangying
     */
    boolean addFluxSensor(FluxSensorForm form);

    /**
         * @param form
     * @return boolean
     * @throws
     * @Title: 批量新增流量传感器
     * @author wangying
     */
    boolean addFluxSensorByBatch(List<FluxSensorForm> list);

    /**
         * @param form
     * @return boolean
     * @throws
     * @Title: 修改流量传感器
     * @author wangying
     */
    boolean updateFluxSensor(FluxSensorForm form);

    /**
         * @param id
     * @return boolean
     * @throws
     * @Title: 删除流量传感器
     * @author wangying
     */
    boolean deleteFluxSensor(String id);

    /**
         * @param sensorId
     * @return boolean
     * @throws
     * @Title: 根据流量传感器id删除车和流量传感器的关联关系
     * @author wangying
     */
    boolean deleteFluxSensorBind(@Param("sensorId") String sensorId);

    /**
         * @param sensorId
     * @return boolean
     * @throws
     * @Title: 根据车辆id删除车和流量传感器的关联关系
     * @author wangying
     */
    boolean deleteFluxSensorBindByVehicleId(@Param("vehicleId") String vehicleId);

    FluxSensor isExist(@Param("id") String id, @Param("number") String number);

    /**
         * @param sensorId
     * @return FluxSensor
     * @throws
     * @Title:根据传感器ID查询绑定关系
     * @author yangyi
     */
    List<FluxSensor> findOilWearByVid(@Param("id") String id);

    /**
     * 批量删除
     * @param monitorIds monitorIds
     * @return
     */
    boolean deleteBatchFluxSensorBindByVehicleId(@Param("monitorIds") List<String> monitorIds);
}
