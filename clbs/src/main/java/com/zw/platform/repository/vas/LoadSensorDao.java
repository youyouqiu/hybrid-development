package com.zw.platform.repository.vas;

import com.zw.platform.domain.vas.loadmgt.ZwMSensorInfo;
import com.zw.platform.domain.vas.loadmgt.form.LoadSensorForm;
import com.zw.platform.domain.vas.loadmgt.form.LoadSensorImportForm;
import com.zw.platform.domain.basicinfo.query.LoadSensorQuery;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSensorForm;

import java.util.List;

/***
 @Author gfw
 @Date 2018/9/6 14:27
 @Description 载重传感器 Dao层
 @version 1.0
 **/
public interface LoadSensorDao {
    /**
     * 列表分页查询
     * @param query
     * @return
     */
    List<ZwMSensorInfo> findListByQuery(final LoadSensorQuery query);

    /**
     * 新增单个传感器
     * @param form
     * @return
     */
    boolean add(LoadSensorForm form);

    /**
     * 根据id查询传感器信息
     * @param id
     * @return
     */
    ZwMSensorInfo findSensorById(String id);

    /**
     * 根据id删除传感器
     * @param id
     * @return
     */
    boolean deleteById(String id);

    /**
     * 根据传感器型号
     * @param sensorNum
     * @return
     */
    List<ZwMSensorInfo> findBySensorNumber(String sensorNum);

    /**
     * 批量插入
     * @param list
     * @return
     */
    boolean addByBatch(List<LoadSensorImportForm> list);

    /**
     * 根据传递的对象修改传感器信息
     * @param form
     * @return
     */
    boolean update(LoadSensorForm form);

    /**
     * 修改传感器信息
     * @param form
     * @return
     */
    boolean updateWorkHourSensorSomeField(LoadSensorForm form);

}
