package com.zw.platform.service.oilmgt;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by Tdz on 2016/9/18.
 */
public interface OilStatisticalService {

    List<Positional> getOilInfo(String band, String startTime, String endTime) throws Exception;

    List<Positional> getAppOilInfo(List<String> bands, String startTime, String endTime) throws Exception;

    List<FuelVehicle> getVehiceInfo(String groupId) throws Exception;

    JSONObject getInfoDtails(List<Positional> oilInfo, String band) throws Exception;

    /**
     * 导出行驶油耗报表
     * @param response
     * @throws Exception
     */
    void exportFuelConsumptionReport(HttpServletResponse response) throws IOException;
}
