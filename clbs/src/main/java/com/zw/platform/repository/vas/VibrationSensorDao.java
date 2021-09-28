package com.zw.platform.repository.vas;

import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorForm;
import com.zw.platform.domain.vas.workhourmgt.query.VibrationSensorQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
public interface VibrationSensorDao {
	/**
	 * 查询振动传感器
	 */
	List<VibrationSensorForm> findVibrationSensor(VibrationSensorQuery query);
	/**
	 * 增加振动传感器
	 * @param form
	 * @return
	 * @author Fan Lu
	 */
	boolean addVibrationSensor(VibrationSensorForm form);
	/**
	 * 删除振动传感器
	 * @param form
	 * @return
	 * @author Fan Lu
	 */
	boolean deleteVibrationSensor(String id);
	/**
	 * 修改振动传感器
	 * @param form
	 * @return
	 * @author Fan Lu
	 */
	boolean updateVibrationSensor(VibrationSensorForm form);
	/**
	 * 查询振动传感器（根据id）
	 * 
	 * @param id
	 * @return
	 * @author Fan Lu
	 */
	VibrationSensorForm findVibrationSensorById(String id);
	/**
	 * 查询振动传感器（根据id）
	 * 
	 * @param id
	 * @return
	 * @author Fan Lu
	 */
	int findByNumber(String sensorNumber);
	
	/**
	* 根据传感器型号查询传感器
	* @Title: findVibrationSensorByType
	* @param type
	* @return
	* @return List<VibrationSensorForm>
	* @throws
	* @author Liubangquan
	 */
	List<VibrationSensorForm> findVibrationSensorByType(String type);
	
	/**
	* 批量新增传感器
	* @Title: addByBatch
	* @param list
	* @return
	* @return boolean
	* @throws
	* @author Liubangquan
	 */
	boolean addByBatch(List<VibrationSensorForm> list);
	int isExist( @Param("id")String id, @Param("sensorNumber")String sensorNumber);
	/**
	*根据传感器型号ID查询绑定关系
	* @Title: addByBatch
	* @param id
	* @returnList<VibrationSensorForm>
	* @return List
	* @throws
	* @author yangyi 
	 */
	List<VibrationSensorForm> findById(@Param("id")String id);
	

}
