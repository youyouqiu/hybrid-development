package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSensorInfo;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourSensorQuery;

import java.util.List;

/**
 * 工时传感器Dao层
 * @author denghuabing
 * @date 2018.5.29
 * @version 1.0
 */
public interface WorkHourSensorDao {

    /**
     * 分页查询
     * @param query
     * @return
     */
    Page<WorkHourSensorInfo> findByPage(WorkHourSensorQuery query);

    /**
     * 新增工时传感器
     * @param form
     * @return
     */
    boolean addWorkHourSensor(WorkHourSensorForm form);

    /**
     * 根据id查询工时传感器信息
     * @param id
     * @return
     */
    WorkHourSensorForm findWorkHourSensorById(String id);

    /**
     * 修改工时传感器
     * @param form
     * @return
     */
    boolean updateWorkHourSensor(WorkHourSensorForm form);

    /**
     * 删除工时传感器
     * @param id
     * @return
     */
    boolean deleteWorkHourSensor(String id);

    /**
     * 批量删除
     * @param workHourIds
     * @return
     */
    boolean deleteMore(String[] workHourIds);

    /**
     * 型号校验
     * @param sensorName
     * @return
     */
    WorkHourSensorForm findWorkHourSensorByName(String sensorName);

    /**
     * 导出excel
     * @return
     */
    List<WorkHourSensorForm> exportWorkHourSensor();

    /**
     * 批量导入
     * @param importList
     * @return
     */
    boolean addWorkHourSensorByBatch(List<WorkHourSensorForm> importList);
}
