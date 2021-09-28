package com.zw.platform.util.validator;

import com.zw.platform.domain.functionconfig.form.FenceConfigForm;
import com.zw.platform.util.common.DateUtil;

import java.util.Date;


/***
 @Author zhengjc
 @Date 2019/5/21 9:39
 @Description 围栏校验工具类
 @version 1.0
 **/
public class FenceValidator {
    private static boolean isStartTimeAfterEndTime(FenceConfigForm config) {
        String startDateStr =
            DateUtil.getLongToDateStr(config.getAlarmStartTime(), "yyyy-MM-dd") + " " + DateUtil
                .getLongToDateStr(config.getAlarmStartDate(), "HH:mm:ss");
        String endDateStr =
            DateUtil.getLongToDateStr(config.getAlarmEndTime(), "yyyy-MM-dd") + " " + DateUtil
                .getLongToDateStr(config.getAlarmEndDate(), "HH:mm:ss");
        Date startDate = DateUtil.getStringToDate(startDateStr, null);
        Date endDate = DateUtil.getStringToDate(endDateStr, null);
        return startDate != null && endDate != null && !startDate.before(endDate);
    }

    private static boolean isNotNullOfAlarmDate(FenceConfigForm config) {
        return config.getAlarmStartDate() != null && config.getAlarmEndDate() != null
               && config.getAlarmStartTime() != null && config.getAlarmEndTime() != null;
    }

    public static boolean isErrorDateParam(FenceConfigForm config) {
        return isNotNullOfAlarmDate(config) && isStartTimeAfterEndTime(config);
    }

}
