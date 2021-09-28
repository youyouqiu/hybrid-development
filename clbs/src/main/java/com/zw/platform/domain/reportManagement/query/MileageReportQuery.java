package com.zw.platform.domain.reportManagement.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

/**
 * @author zhouzongbo on 2018/12/11 18:05
 */
@Data
public class MileageReportQuery extends BaseQueryBean {

    private static final long serialVersionUID = 7615707291204870655L;

    private String VehicleId;

    private String startTime;

    private String endTime;
}
