/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of 
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered into with ZhongWei.
 */
package com.zw.platform.service.carbonmgt;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.domain.vas.carbonmgt.form.TimeEnergyStatisticsForm;
import com.zw.platform.domain.vas.carbonmgt.query.TimeEnergyStatisticsQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 时间能耗统计Service
 * <p>Title: TimeEnergyStatisticsService.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年9月19日下午5:14:54
 * @version 1.0
 * 
 */
public interface TimeEnergyStatisticsService {
	
	/**
	* 时间能耗列表-按日期查询
	* @Title: queryByDate
	* @param query
	* @return
	* @return List<TimeEnergyStatisticsForm>
	* @throws BusinessException
	* @author Liubangquan
	 */
	public List<TimeEnergyStatisticsForm> queryByDate(String queryWay, String groupId, String startDate, String endDate, String vehicleId) throws BusinessException;
	
	/**
	* 获取车辆列表-能耗列表查询时用
	* @Title: getVehicleInfoList
	* @return
	* @throws BusinessException
	* @return List<VehicleInfo>
	* @throws
	* @author Liubangquan
	 */
	public List<VehicleInfo> getVehicleInfoList(String groupId) throws BusinessException;
	
	/**
	* 时间能耗导出功能
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
	public boolean export(String title, int type, HttpServletResponse response, TimeEnergyStatisticsQuery query);

}
