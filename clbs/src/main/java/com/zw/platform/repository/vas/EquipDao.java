package com.zw.platform.repository.vas;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.carbonmgt.form.EquipForm;
import com.zw.platform.domain.vas.carbonmgt.query.EquipQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EquipDao {

	/**
	 * 查询基准信息（根据用户）
	 * 
	 * @param groupId query
	 * @return
	 * @author Fan Lu
	 */
	List<EquipForm> findBenchmark(@Param("groupList") List<String> groupId, @Param("param") EquipQuery query);
	/**
	 * 增加基准信息
	 * @param form
	 * @return
	 * @author Fan Lu
	 */
	boolean addBenchmark(EquipForm form);
	/**
	 * 删除基准信息
	 * @param form
	 * @return
	 * @author Fan Lu
	 */
	boolean deleteBenchmark(EquipForm form);
	/**
	 * 修改基准信息
	 * @param form
	 * @return
	 * @author Fan Lu
	 */
	boolean updateBenchmark(EquipForm form);
	/**
	 * 查询企业下分组的车
	 * 
	 * @param id groupId
	 * @return
	 * @author Fan Lu
	 */
	List<VehicleInfo> findVehicleByUser(@Param("groupList") List<String> groupId);
	/**
	 * 查询分组下的车
	 * 
	 * @param id groupId
	 * @return
	 * @author Fan Lu
	 */
	List<VehicleInfo> findVehicleByAssign(@Param("groupList") List<String> groupId);
	/**
	 * 查询基准信息（根据id）
	 * 
	 * @param id
	 * @return
	 * @author Fan Lu
	 */
	EquipForm findBenchmarkById(String id);
	
	/**
	* 根据车辆id查询基准信息
	* @Title: findBenchmarkByVehicleId
	* @param vehicleId
	* @return
	* @return EquipForm
	* @throws
	* @author Liubangquan
	 */
	EquipForm findBenchmarkByVehicleId(String vehicleId);
	
}
