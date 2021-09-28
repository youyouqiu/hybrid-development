package com.zw.platform.util.privilege;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.util.common.Date8Utils;
import com.zw.ws.common.PublicVariable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrgShowUtils {

    @Autowired
    private UserPrivilegeUtil userPrivilegeUtil;
    private static final Logger log = LogManager.getLogger(OrgShowUtils.class);

    /**
     * esjob相关计算模块的key
     */
    private static final String CUSTOMER_SERVICE_JOB = "customer_service_job";

    private static final String VEHICLE_ONLINE_JOB = "vehicle_online_job";

    private static final String VEHICLE_EVENT_JOB = "vehicle_event_job";

    private static final String VEHICLE_EVENT_HOUR_JOB = "vehicle_event_hour_job";

    private static final String RISK_WARING_JOB = "risk_waring_job";

    private static final String RISK_TREND_JOB = "risk_trend_job";

    private static final String RISK_LEVEL_JOB = "risk_level_job";

    /**
     * 领导看板查询模块相关的key
     */

    public static final String EVENT_RANKING = "event_ranking";

    public static final String RISK_PROPORTION = "risk_proportion";

    public static final String RISK_TYPE_TREND = "risk_type_trend";

    public static final String EVENT_TREND = "event_trend";

    public static final String VEH_ONLINE_TREND = "veh_online_trend";

    public static final String RISK_DEAL_INFO = "risk_deal_info";

    public static final String CUSTOMER_SERVICE_TREND = "customer_service_trend";

    public void storeCacheToRedis(OrgShowQuery orgShowQuery) {

        String module = orgShowQuery.getModule();
        try {

            RedisKey moduleKey = getLbModuleKey(module, orgShowQuery.getDayKey());
            String storeData = JSONObject.toJSONString(orgShowQuery.getResult());
            if (RedisHelper.isContainsKey(moduleKey)) {
                RedisHelper
                    .addToHash(moduleKey, getFieldKey(orgShowQuery.getGroupId(), orgShowQuery.getTimeKey()), storeData);
            } else {
                RedisHelper
                    .addToHash(moduleKey, getFieldKey(orgShowQuery.getGroupId(), orgShowQuery.getTimeKey()), storeData);
                RedisHelper.expireKey(moduleKey, PublicVariable.REDIS_CACHE_TIMEOUT_DAY);
            }
        } catch (Exception e) {
            log.error(module + "模块缓存存储失败！", e);
        }
    }

    private RedisKey getLbModuleKey(String module, long day) {
        return userPrivilegeUtil.getAdasReportCachePrefix("_" + module + "_" + day);
    }

    private static String getFieldKey(String groupId, long time) {
        StringBuilder sb = new StringBuilder();
        groupId = groupId != null ? groupId : "";
        sb.append(time).append("_").append(groupId);
        return sb.toString();
    }

    public static long getValToHour(LocalDateTime dateTime, boolean isToday) {
        return isToday ? Date8Utils.getValToHour(dateTime) : Date8Utils.getMidnightHour(dateTime);
    }

    public <T> List<T> getDataByTemplate(OrgShowQuery<T> orgShowQuery) {
        List<T> result;
        LocalDateTime dateTime = LocalDateTime.now();
        //定时任务移交后实时数据，需注释下列代码
        // if (!orgLbJobIsDone(dateTime)) {
        //     dateTime = dateTime.minusHours(1);
        // }
        orgShowQuery.setDateTime(dateTime);
        result = orgShowQuery.queryAndSetResult();
        storeCacheToRedis(orgShowQuery);
        return result;
    }

    private static String getOrgShowEsJobKey(int shard, LocalDateTime dateTime, String module) {
        return Date8Utils.getValToHour(dateTime) + "_" + module + "_" + shard;
    }

    private static String getOrgShowEsJobKey(LocalDateTime dateTime, String module) {
        return Date8Utils.getValToHour(dateTime) + "_" + module;
    }

    private static String[] getOrgLbJobKeys(LocalDateTime dateTime, int orgVidShardNum) {
        List<String> orgShowKeys = new ArrayList<>();
        orgShowKeys.add(getOrgShowEsJobKey(dateTime, CUSTOMER_SERVICE_JOB));
        orgShowKeys.add(getOrgShowEsJobKey(dateTime, VEHICLE_ONLINE_JOB));
        for (int i = 0; i < orgVidShardNum; i++) {
            orgShowKeys.add(getOrgShowEsJobKey(i, dateTime, VEHICLE_EVENT_JOB));
            orgShowKeys.add(getOrgShowEsJobKey(i, dateTime, VEHICLE_EVENT_HOUR_JOB));
            orgShowKeys.add(getOrgShowEsJobKey(i, dateTime, RISK_WARING_JOB));
            orgShowKeys.add(getOrgShowEsJobKey(i, dateTime, RISK_TREND_JOB));
            orgShowKeys.add(getOrgShowEsJobKey(i, dateTime, RISK_LEVEL_JOB));
        }
        return orgShowKeys.toArray(new String[] {});
    }

    private int getOrgVidShardNum(LocalDateTime dateTime) {
        String orgVidShardNum =
            RedisHelper.getString(HistoryRedisKeyEnum.ADAS_ORG_VID_CACHE.of(Date8Utils.getValToHour(dateTime)));
        return orgVidShardNum == null ? 0 : Integer.parseInt(orgVidShardNum);
    }

}
