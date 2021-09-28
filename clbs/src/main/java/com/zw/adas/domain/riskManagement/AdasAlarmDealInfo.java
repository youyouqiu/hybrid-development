package com.zw.adas.domain.riskManagement;

import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.util.StrUtil;
import lombok.Data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/***
 @Author zhengjc
 @Date 2019/7/26 14:05
 @Description 主动安全809报警处理接口
 @version 1.0
 **/
@Data
public class AdasAlarmDealInfo {

    /**
     * 主动安全报警的id
     */
    private String riskIds;
    /**
     * 主动安全报警事件的id
     */
    private String eventIds;
    /**
     * 主动安全报警事件的集合
     */
    private Set<String> eventIdSet;

    /**
     * 报警处理结果：事故已发生1和事故未发生0
     */
    private Integer riskResult;

    /**
     * 报警处理方式
     */
    private String handleType;

    /**
     * 809转发模块处理弹出框备注
     */
    private String remark;

    public static AdasAlarmDealInfo getInstance(String riskIds, String eventIds, Integer riskResult,
        String handleType, String remark) {
        AdasAlarmDealInfo dealInfo = new AdasAlarmDealInfo();
        dealInfo.eventIds = eventIds;
        dealInfo.riskIds = riskIds;
        dealInfo.riskResult = riskResult;
        dealInfo.handleType = handleType;
        dealInfo.remark = remark;

        if (StrUtil.isNotBlank(eventIds)) {
            dealInfo.eventIdSet = new HashSet<>();
            dealInfo.eventIdSet.addAll(Arrays.asList(eventIds.split(",")));
        }
        return dealInfo;
    }



    public static AdasAlarmDealInfo getInstance(String eventIds, String handleType) {
        return getInstance(null, eventIds, null, handleType, null);
    }

    public static AdasAlarmDealInfo getInstance(HandleAlarms handleAlarms) {
        return getInstance(handleAlarms.getRiskId(), handleAlarms.getRiskEventId(), null,
            handleAlarms.getHandleType(), handleAlarms.getRemark());
    }

}
