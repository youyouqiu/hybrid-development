package com.zw.platform.repository.energy;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.energy.Energy;

public interface EnergyDao {
	int deleteByPrimaryKey(String id);

	int insert(Energy energy);

	int insertSelective(Energy energy);

	Energy selectByPrimaryKey(String id);

	Energy selectByVehicleId(String vehicleId);

	List<Energy> selectByCondition(Energy energy);

	void deleteBatch(List<String> list);

	int updateByVehicleId(Energy energy);

	int updateByPrimaryKey(Energy energy);

	/**
	 * 根据监控对象id解除移动源基础设置
	 * @param vehicleId
	 * @return
	 */
	boolean changeRelieveEnergy(String vehicleId);

	/**
	 * 根据车辆id查询节油产品安装时间
	 * 
	 * @param vehicleId
	 * @return
	 */
	Date findInstallTime(String vehicleId);

	/**
	 * 查询所有设置了基准的车辆能耗基础数据
	 * 
	 * @return
	 */
	List<Energy> findIsSetEnergy();

	Energy findEnergyAndFuelTypeByVehicleId(@Param("vehicleId") String vehicleId);

	VehicleInfo findIsVehicleAllByVehicleId(@Param("vehicleId") String vehicleId);

	/**
	 * 根据车辆id查询车牌号
	 * 
	 * @param vehicleId
	 * @return
	 */
	String findBrandByVehicleId(@Param("vehicleId") String vehicleId);
}