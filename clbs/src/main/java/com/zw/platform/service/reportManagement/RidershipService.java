package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.reportManagement.Ridership;
import com.zw.platform.domain.reportManagement.query.RidershipQuery;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

/**
 * @author zhangsq
 * @date 2018/3/23 9:07
 */
public interface RidershipService {

    void add(String vehicleId, String starTime, String endTime, Integer onTheTrain, Integer getOffTheCar)
        throws ParseException;

    List<Ridership> findByVehicleIdAndDate(RidershipQuery ridershipQuery);

    boolean export(String o, int i, HttpServletResponse res) throws Exception;
}
