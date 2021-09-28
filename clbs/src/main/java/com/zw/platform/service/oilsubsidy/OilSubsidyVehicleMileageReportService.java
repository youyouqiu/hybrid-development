package com.zw.platform.service.oilsubsidy;

import com.zw.platform.domain.oilsubsidy.mileagereport.OilSubsidyVehicleMileMonthVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author XK
 */
public interface OilSubsidyVehicleMileageReportService {
    /**
     * 获取车辆日里程统计
     * @param vehicleIds 车俩id
     * @param month      年月
     * @return 车辆日里程统计
     */
    List<OilSubsidyVehicleMileMonthVO> getVehicleMileMonths(Collection<String> vehicleIds, String month)
        throws Exception;

    /**
     * 导出车辆月统计报表
     * @param o   o
     * @param i   i
     * @param res res
     */
    void exportVehicleMonth(String o, int i, HttpServletResponse res) throws IOException;

    /**
     * 由MILEAGE_STATISTIC_{month}获取的数据可能存在历史数据, 3.8.2版本如果sensorFlag = 0, 则表示未绑定传感器milage=gpsMile,
     * @param sensorFlag sensorFlag =1则表示绑定了里程传感器
     * @param mileage    传感器里程
     * @param gpsMile    gps里程
     * @return 里程
     */
    Double getMile(Integer sensorFlag, Double mileage, Double gpsMile);
}
