package com.zw.adas.domain.riskManagement.query;

import com.google.common.collect.Lists;
import com.zw.platform.util.common.BaseQueryBean;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by PengFeng on 2017/8/24  18:02
 */
@EqualsAndHashCode(callSuper = false)
public class AdasRiskDisposeRecordQuery extends BaseQueryBean {

    @Getter
    @Setter
    private String eventIdStrs;

    @Getter
    @Setter
    private List<byte[]> eventIds;

    @Getter
    @Setter
    private String riskIdStrs;

    @Getter
    @Setter
    private List<byte[]> riskIds;

    @Getter
    @Setter
    private String riskIdStr;

    @Getter
    @Setter
    private byte[] riskId;

    @Getter
    @Setter
    private String vehicleIds;

    @Getter
    @Setter
    private String startTime;

    @Getter
    @Setter
    private String endTime;

    @Getter
    @Setter
    private String riskNumber;

    @Getter
    @Setter
    private String riskType;

    @Getter
    @Setter
    private String riskLevel;

    @Getter
    @Setter
    private String brand;

    @Getter
    @Setter
    private String driver;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private String dealUser;

    @Getter
    @Setter
    private String visitTime;

    @Getter
    @Setter
    private String riskResult;

    @Getter
    @Setter
    private String evidenceType;

    @Getter
    @Setter
    private String riskEvent;

    @Setter
    @Getter
    private String[] deleteIds;

    @Setter
    @Getter
    private Object[] searchAfter;

    @Setter
    @Getter
    private List<String> excludeIds = Lists.newLinkedList();

}

