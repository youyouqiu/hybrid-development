package com.cb.platform.domain;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


public class VehicleTravelQuery extends BaseQueryBean {

    @Getter
    @Setter
    private String vehicleIds;

    @Getter
    @Setter
    private String travelId;

    @Getter
    @Setter
    private String brand;

    @Getter
    @Setter
    private List<String> vehicleIdList;

}
