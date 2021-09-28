package com.zw.platform.service.generalCargoReport;

import com.github.pagehelper.Page;
import com.zw.platform.domain.generalCargoReport.CargoSpotCheckForm;
import com.zw.platform.domain.generalCargoReport.CargoSearchQuery;

import java.util.List;

public interface SpotCheckCargoReortService {

    Boolean batchDeal(String dealMeasure, String dealResult) throws Exception;

    Page<CargoSpotCheckForm> searchSpotCheck(CargoSearchQuery cargoSpotCheckQuery) throws Exception;

    List<CargoSpotCheckForm> exportSearchSpotCheck(CargoSearchQuery cargoSpotCheckQuery) throws Exception;

}
