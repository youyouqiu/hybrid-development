package com.zw.platform.service.generalCargoReport;

import com.github.pagehelper.Page;
import com.zw.platform.domain.generalCargoReport.CargoMonthReportInfo;
import com.zw.platform.domain.generalCargoReport.CargoSearchQuery;

import java.util.List;

public interface MonthReportCargoService {

    Page<CargoMonthReportInfo> searchMonthData(CargoSearchQuery cargoSpotCheckQuery) throws Exception;

    List<CargoMonthReportInfo> exportSearchMonthData(CargoSearchQuery cargoSpotCheckQuery) throws Exception;
}
