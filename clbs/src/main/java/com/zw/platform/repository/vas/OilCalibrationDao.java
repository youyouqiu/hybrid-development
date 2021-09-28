/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of 
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered into with ZhongWei.
 */
package com.zw.platform.repository.vas;

import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.LastOilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 油量标定Dao
 * <p>Title: OilCalibrationDao.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年12月14日下午2:20:53
 * @version 1.0
 */
public interface OilCalibrationDao {

	/**
	* 查询车辆下拉列表（绑定了*_油箱、油杆传感器，在线_*的车辆）
	* @Title: getVehicleList
	* @param userId
	* @param groupList
	* @return
	* @return List<OilVehicleSetting>
	* @throws
	* @author Liubangquan
	 */
	public List<OilVehicleSetting> getVehicleList(@Param("userId") String userId, @Param("groupList") List<String> groupList);
	
	/**
	* 根据车辆id查询与车辆绑定的油箱的标定数据
	* @Title: getOilCalibrationByVid
	* @param vehicleId
	* @return
	* @return List<OilCalibrationForm>
	* @throws
	* @author Liubangquan
	 */
	public List<OilCalibrationForm> getOilCalibrationByVid(String vehicleId);
	
	/**
	* 根据oilBoxVehicleId删除标定数据
	* @Title: deleteOilCalibrationByOilBoxVehicleId
	* @param oilBoxVehicleId
	* @return
	* @return boolean
	* @throws
	* @author Liubangquan
	 */
	public void deleteOilCalibrationByOilBoxVehicleId(String oilBoxVehicleId);
	
	/**
	* 保存标定数据
	* @Title: addOilCalibration
	* @param form
	* @return
	* @return boolean
	* @throws
	* @author Liubangquan
	 */
	public void addOilCalibration(OilCalibrationForm form);
	
	/**
	* 根据车辆id查询标定状态数据
	* @Title: getCalibrationStatusByVid
	* @param vehicleId
	* @return
	* @return String
	* @throws
	* @author Liubangquan
	 */
	public List<String> getCalibrationStatusByVid(String vehicleId);
	
	/**
	* 重置标定状态
	* @Title: updateCalibrationStatusByVid
	* @param vehicleId
	* @param calibrationStatus
	* @param upateTime
	* @param updateUserName
	* @return
	* @return boolean
	* @throws
	* @author Liubangquan
	 */
	public boolean updateCalibrationStatusByVid(@Param("vehicleId") String vehicleId, @Param("calibrationStatus") String calibrationStatus,
			@Param("updateTime") String upateTime, @Param("updateUserName") String updateUserName);
	
	/**
	* 判断车辆是否绑定油箱和传感器
	* @Title: checkIsBondOilBox
	* @param vehicleId
	* @return
	* @return boolean
	* @throws
	* @author Liubangquan
	 */
	public int checkIsBondOilBox(String vehicleId);
	
	/**
	* 保存车辆最后一次标定的时间-追溯标定用
	* @Title: saveLastCalibration
	* @param form
	* @return void
	* @throws
	* @author Liubangquan
	 */
	public void saveLastCalibration(LastOilCalibrationForm form);
	
	/**
	* 根据车辆id删除车辆最后 次标定的时间
	* @Title: deleteLastCalibration
	* @param vehicleId
	* @return void
	* @throws
	* @author Liubangquan
	 */
	public void deleteLastCalibration(String vehicleId);
	
	/**
	* 根据车辆id获取车辆最后一次标定的时间
	* @Title: getLastCalibration
	* @param vehicleId
	* @return
	* @return List<LastOilCalibrationForm>
	* @throws
	* @author Liubangquan
	 */
	public List<LastOilCalibrationForm> getLastCalibration(String vehicleId);
	
	/**
	* 获取标定状态为占用状态的更新时间
	* @Title: getCalibrationUpdateTimeByVid
	* @param vehicleId
	* @return
	* @return String
	* @throws
	* @author Liubangquan
	 */
	public List<String> getCalibrationUpdateTimeByVid(String vehicleId);
	
}
