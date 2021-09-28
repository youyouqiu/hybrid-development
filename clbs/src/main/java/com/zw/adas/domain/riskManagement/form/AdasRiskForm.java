package com.zw.adas.domain.riskManagement.form;

import com.zw.platform.util.common.UuidUtils;
import lombok.Data;


@Data
public class AdasRiskForm {
    private String address;

    private String brand;

    private Long dealTime;//需转

    private String dealTimeStr;

    private String dealUser;

    private String driver;

    private Long fileTime;//需转

    private String fileTimeStr;

    private String formattedAddress;

    private byte[] id;

    private String idStr;

    private String job;

    private Integer riskLevel;

    private String riskNumber;

    private Integer riskResult;

    private String riskType;

    private Integer status;

    private String vehicleId;

    private Integer visitTimes;

    private Long warTime;//需转

    private String warTimeStr;

    private String driverIds;//司机ids

    private String visit1;

    private String visit2;

    private String visit3;

    private String visit4; //归档的回访

    private String handleType;

    public static AdasRiskForm of(Integer riskResult, String dealUser, long dealTime, String riskId,
                                   String handleType) {
        final AdasRiskForm form = new AdasRiskForm();
        form.setId(UuidUtils.getBytesFromStr(riskId));
        form.setStatus(6);
        form.setDealUser(dealUser);
        form.setFileTime(dealTime);
        form.setRiskResult(riskResult);
        form.setDealTime(dealTime);
        form.setHandleType(handleType);
        return form;
    }

}
