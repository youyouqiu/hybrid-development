package com.zw.platform.repository.modules;


import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.form.RodSensorForm;
import com.zw.platform.domain.basicinfo.form.RodSensorImportForm;
import com.zw.platform.domain.basicinfo.query.RodSensorQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 油杆传感器 Created by Tdz on 2016/7/25.
 */
public interface RodSensorDao {
    RodSensor get(final String id);

    /**
     * 查询
     */
    List<RodSensor> find(final RodSensorQuery query);

    /**
     * 查询
     */
    List<RodSensor> findAllow();

    /**
     * 新增
     */
    boolean add(final RodSensorForm form);

    /**
     * 根据id删除一个
     */
    boolean delete(final String id);

    /**
     * 修改
     */
    boolean update(final RodSensorForm form);

    /**
     * 修改 波特率/奇偶效验
     * @param form
     * @return
     */
    Integer updateParamById(final RodSensorForm form);

    List<RodSensor> findBySensorNumber(@Param("sensorNumber") String sensorNumber);

    /**
     * 批量新增
     * @param rodSensorForms
     * @return
     */
    boolean addByBatch(List<RodSensorImportForm> rodSensorImportForms);

    RodSensor findByRodSensor(String sensorNumber);

    public int getIsBand(String id);

    /**
     * 根据id查询传感器信息
     * @Title: findById
     * @param id
     * @return
     * @return RodSensor
     * @throws @author
     *             Liubangquan
     */
    public RodSensor findById(String id);

    RodSensor isExist(@Param("id") String id, @Param("sensorNumber") String sensorNumber);
}
