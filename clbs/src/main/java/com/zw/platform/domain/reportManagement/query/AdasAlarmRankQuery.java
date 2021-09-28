package com.zw.platform.domain.reportManagement.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class AdasAlarmRankQuery extends BaseQueryBean {

    private String startTime;

    private String endTime;

    private String[] vehicleIds;

    private List<String> assignmentIds;

    private String[] groupIds;

    private String gids;

    private String param;

    private List<String> parmGroupIds;

    private String vids;

}
