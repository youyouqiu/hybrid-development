package com.zw.platform.service.carbonmgt;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.domain.vas.carbonmgt.form.MobileSourceEnergyReportForm;
import com.zw.platform.domain.vas.carbonmgt.query.MileageQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 里程能耗报表Service
 * <p>Title: MileageEnergyReportService.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2017年2月17日上午10:18:09
 * @version 1.0
 */
public interface MileageEnergyReportService {

	/**
	* 根据组织id查询组织下的非工程车辆
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
	* 查询油耗数据
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
	* 导出功能
	* @Title: exportMileage
	* @param title
	* @param type
	* @param response
	* @param query
	* @return
	* @return boolean
	* @throws
	* @author Liubangquan
	 */
	boolean exportMileage(String title, int type, HttpServletResponse response,MileageQuery query) throws Exception;

}
