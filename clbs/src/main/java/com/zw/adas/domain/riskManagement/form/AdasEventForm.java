package com.zw.adas.domain.riskManagement.form;

import com.zw.platform.util.common.UuidUtils;
import lombok.Data;

@Data
public class AdasEventForm {
    private String idStr;

    private byte[] id;

    private String eventNumber;

    private Long eventTime;

    private String eventTimeStr;

    private String riskEvent;

    private String brand;

    private String riskType;

    private Integer riskLevel;

    private Long warnTime;

    private String warnTimeStr;

    private Long fileTime;

    private String fileTimeStr;

    private String address;

    private String driverName;

    private Integer status;

    private String dealer;

    private String job;

    private Long dealTime;

    private String dealTimeStr;

    private Integer visitTimes;

    private Integer result;

    private String riskNumber;

    private String vehicleId;

    private String riskId;

    private String handleType;

    private String remark;

    public static AdasEventForm of(
            int status, String dealer, long dealTime, Integer result, String id, String handleType, String remark) {
        final AdasEventForm form = new AdasEventForm();
        form.setId(UuidUtils.getBytesFromStr(id));
        form.setStatus(status);
        form.setDealer(dealer);
        form.setFileTime(dealTime);
        form.setResult(result);
        form.setDealTime(dealTime);
        form.setHandleType(handleType);
        form.setRemark(remark);
        return form;
    }

}
