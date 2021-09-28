package com.cb.platform.domain.sichuan.vehicleonlinerate;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/7 10:56
 */
@Data
public class VehicleOnlineTimeSectionDO {
    /**
     * 在线时间段开始时间(格式:yyyyMMddHHmmss)
     */
    private String timeSectionStartTime;

    /**
     * 在线时间段结束时间(格式:yyyyMMddHHmmss)
     */
    private String timeSectionEndTime;

    /**
     * 定位条数
     */
    private Integer locationNum;
}
