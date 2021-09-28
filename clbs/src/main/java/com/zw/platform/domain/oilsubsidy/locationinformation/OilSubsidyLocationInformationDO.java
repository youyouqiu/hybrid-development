package com.zw.platform.domain.oilsubsidy.locationinformation;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * @author XK
 */
@Data
public class OilSubsidyLocationInformationDO {

    /**
     *定位信息id
     */
    private String id;

    /**
     *转发平台对应的企业id
     */
    private String forwardOrgId;

    /**
     *开始时间
     */
    private Date startTime;

    /**
     *结束时间
     */
    private Date endTime;

    /**
     *油补平台收到的定位数据量
     */
    private Long locationNum;

    public static OilSubsidyLocationInformationDO getInstance(String forwardOrgId, Long startTime, Long endTime,
                                                              Long locationNum) {
        OilSubsidyLocationInformationDO oilSubsidyLocationInformationDO = new OilSubsidyLocationInformationDO();
        oilSubsidyLocationInformationDO.setId(UUID.randomUUID().toString());
        oilSubsidyLocationInformationDO.setForwardOrgId(forwardOrgId);
        oilSubsidyLocationInformationDO.setStartTime(new Date(startTime));
        oilSubsidyLocationInformationDO.setEndTime(new Date(endTime));
        oilSubsidyLocationInformationDO.setLocationNum(locationNum);
        return oilSubsidyLocationInformationDO;
    }

}
