package com.zw.platform.domain.expireRemind;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisKey;

/***
 @Author zhengjc
 @Date 2019/11/27 10:01
 @Description 到期提醒常量
 @version 1.0
 **/
public class ExpireRemindInstant {
    /**
     * 服务即将到期 0
     */
    public static final String expireLifeCycleRemind = "lifecycleExpire_remind_list";
    /**
     * 服务已经到期 1
     */
    public static final String alreadyExpireLifeCycle = "already_lifecycleExpire_list";
    /**
     * 驾驶证即将到期 2
     */
    public static final String expireDrivingLicense = "expireDrivingLicense_list";
    /**
     * 驾驶证已经到期  3
     */
    public static final String alreadyExpireDrivingLicense = "alreadyExpireDrivingLicense_list";
    /**
     * 道路运输证即将到期 4
     */
    public static final String expireRoadTransport = "expireRoadTransport_list";
    /**
     * 道路运输证已经到期 5
     */
    public static final String alreadyExpireRoadTransport = "alreadyExpireRoadTransport_list";
    /**
     * 保养即将到期 6
     */
    public static final String expireMaintenance = "expireMaintenance_list";
    /**
     * 保险即将到期 7
     */
    public static final String expireInsurance = "expireInsurance_list";

    /**
     * 即将到期的保险单id(存储方式不一样)
     */
    public static final String expireInsuranceId = "expireInsuranceId_list";

    public static final String expireLifeCycle = "lifecycleExpire_list";

    private static final RedisKey[] moduleName =
        { HistoryRedisKeyEnum.EXPIRE_LIFE_CYCLE_REMIND.of(), HistoryRedisKeyEnum.ALREADY_EXPIRE_LIFE_CYCLE.of(),
            HistoryRedisKeyEnum.EXPIRE_DRIVING_LICENSE.of(), HistoryRedisKeyEnum.ALREADY_EXPIRE_DRIVING_LICENSE.of(),
            HistoryRedisKeyEnum.EXPIRE_ROAD_TRANSPORT.of(), HistoryRedisKeyEnum.ALREADY_EXPIRE_ROAD_TRANSPORT.of(),
            HistoryRedisKeyEnum.EXPIRE_MAINTENANCE.of(), HistoryRedisKeyEnum.EXPIRE_INSURANCE.of() };

    public static RedisKey getExpireKey(int type) {
        return moduleName[type];
    }
}
