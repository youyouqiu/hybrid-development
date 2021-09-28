package com.zw.platform.service.singleVehicle;


import com.zw.platform.domain.reportManagement.TerminalMileageDailyDetails;
import com.zw.platform.domain.singleVehicle.SingleVehicleAcount;
import com.zw.platform.domain.vas.alram.query.SingleVehicleAlarmSearchQuery;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

import javax.servlet.http.HttpServletRequest;
import java.time.YearMonth;
import java.util.List;

public interface SingleVehicleService {

    JsonResultBean addLogAndSingleVehicleLogin(String brand, String vehiclePassword, HttpServletRequest request);

    JsonResultBean getSingleVehicleLocation(String brand);

    JsonResultBean getSingleVehicleHistoryData(String monitorId, String startTime,
                                         String endTime, Integer sensorFlag) throws Exception;

    /**
     * 报警查询
     * @param query 查询条件
     * @return JsonResultBean
     */
    JsonResultBean queryAlarmList(SingleVehicleAlarmSearchQuery query, String brand);

    /**
     * 报警分页
     * @param query 查询条件
     * @param brand 车牌号
     * @return page
     */
    PageGridBean getPage(SingleVehicleAlarmSearchQuery query, String brand);

    JsonResultBean getMaintenanceReminder(String brand);

    JsonResultBean updatePassword(SingleVehicleAcount acount, String brand, String token);

    /**
     * 单车登录小程序日志
     * @param brand 车牌号
     * @param ip IP地址
     */
    void addLog(String brand, String ip, String message);

    /**
     * 登出
     * @param token token
     * @param brand 车牌号
     * @return JsonResultBean j
     */
    JsonResultBean logOut(String token, String brand);

    /**
     * 查询单车单月里程报表每日明细
     */
    List<TerminalMileageDailyDetails> listMonthTerminalMileageDetail(String brand, YearMonth month);
}
