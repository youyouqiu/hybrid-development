package com.zw.platform.service.carbonmgt;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceEnergyReportForm;
import com.zw.platform.domain.vas.carbonmgt.query.MileageQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface EnergySavingProductsDataBeforeService {

	/**
	* 根据组织id查询组织下面的车辆
	* @Title: getVehicleInfoList
	* @param groupId
	* @return
	* @throws BusinessException
	* @return List<VehicleInfo>
	* @throws
	* @author Liubangquan
	 */
	public List<VehicleInfo> getVehicleInfoList(String groupId) throws Exception;

	/**
	* 数据查询
	* @Title: queryByDate
	* @param queryWay
	* @param groupId
	* @param startDate
	* @param endDate
	* @param vehicleId
	* @return
	* @return List<MobileSourceEnergyReportForm>
	* @throws
	* @author Liubangquan
	 */
	public List<MobileSourceEnergyReportForm> queryByDate(String queryWay, String groupId,String startDate, String endDate, String vehicleId) throws Exception;
	
	/**
	* 导出
	* @Title: export
	* @param title
	* @param type
	* @param response
	* @param query
	* @return
	* @return boolean
	* @throws
	* @author Liubangquan
	 */
	boolean export(String title, int type, HttpServletResponse response,MileageQuery query)throws Exception;

}
