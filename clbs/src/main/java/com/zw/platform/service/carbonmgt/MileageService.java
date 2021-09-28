package com.zw.platform.service.carbonmgt;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.domain.vas.carbonmgt.form.MileageForm;
import com.zw.platform.domain.vas.carbonmgt.query.MileageQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface MileageService {

	public List<VehicleInfo> getVehicleInfoList(String groupId) throws BusinessException;

	public List<MileageForm> queryByDate(String queryWay, String groupId,String startDate, String endDate, String vehicleId);
	
	/**
     * 导出
     */
	boolean exportMileage(String title, int type, HttpServletResponse response,MileageQuery query);

}
