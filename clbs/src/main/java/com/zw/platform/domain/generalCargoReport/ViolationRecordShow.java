package com.zw.platform.domain.generalCargoReport;

import com.zw.platform.domain.oil.AlarmHandle;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.UuidUtils;
import joptsimple.internal.Strings;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/***
 @Author zhengjc
 @Date 2019/9/4 14:31
 @Description 违章记录报表逻辑
 @version 1.0
 **/
@Data
public class ViolationRecordShow extends BaseAddressShow {
    /**
     * 企业名称
     */
    private String groupName;
    /**
     * 时间
     */
    private String alarmStartTime;
    /**
     * 车牌号
     */
    private String brand;
    /**
     * 驾驶员
     */
    private String driver = "";
    /**
     * 监控平台显示位置
     */
    private String address = "";
    /**
     * 违章报警事由
     */
    private String violationReason;
    /**
     * 短信简要提醒内容
     */
    private String msg = "";
    /**
     * 违章处置情况
     */
    private String dealInfo = "";
    /**
     * GPS故障及异常类型
     */
    private String gpsInfo = "";
    /**
     * GPS故障及异常处置情况
     */
    private String gpsDealInfo = "";

    private transient String vehicleId;

    private transient long alarmTime;

    private transient int alarmType;

    /**
     * 导出专用字段
     */

    private String alarmStartTimeHour;
    private String alarmStartTimeMinute;
    private String otherAlarm;
    private String overSpeedAlarm;
    private String tiredAlarm;

    public static ViolationRecordShow getInstance(AlarmHandle alarm) {
        ViolationRecordShow show = new ViolationRecordShow();
        show.vehicleId = UuidUtils.getUUIDStrFromBytes(alarm.getVehicleIdHbase());
        show.alarmStartTime = DateUtil.getLongToDateStr(alarm.getAlarmStartTime(), "HH:mm:ss");
        show.alarmStartLocation = alarm.getAlarmStartLocation();
        show.violationReason = tansViolationReason(alarm.getAlarmType());
        show.dealInfo = StrUtil.getOrBlank(alarm.getHandleType());
        //短信优先展现联动下发的短信内容，其次才是报警处理短信下发的内容
        show.msg = Optional.ofNullable(alarm.getSendOfMsg()).orElse(StrUtil.getOrBlank(alarm.getDealOfMsg()));
        //其他字段
        show.alarmTime = alarm.getAlarmStartTime();
        show.alarmType = alarm.getAlarmType();

        return show;
    }

    private static String tansViolationReason(int alarmType) {
        String result = "其他";
        if (alarmType == 1) {
            result = "超速";
        } else if (alarmType == 2) {
            return "疲劳";
        }
        return result;
    }

    public void initGpsDealInfo(Map<String, Set<String>> gpsShowMaps) {
        if (isGpsAlarm(alarmType)) {
            gpsDealInfo = dealInfo;
            gpsInfo = getAlarmTypeName(alarmType);

        } else {
            if (gpsShowMaps == null || gpsShowMaps.isEmpty()) {
                return;
            }
            Set<String> gpsRecordShows = gpsShowMaps.get(vehicleId + "_" + alarmTime);
            List<String> gpsInfoList = new ArrayList<>();
            List<String> gpsDealInfoList = new ArrayList<>();

            if (CollectionUtils.isEmpty(gpsRecordShows)) {
                return;
            }
            String[] alarmTypeAndHandleType;
            for (String show : gpsRecordShows) {
                alarmTypeAndHandleType = show.split("_");
                gpsInfoList.add(getAlarmTypeName(Integer.parseInt(alarmTypeAndHandleType[0])));
                if (alarmTypeAndHandleType.length == 2 && StrUtil.isNotBlank(alarmTypeAndHandleType[1])) {
                    gpsDealInfoList.add(alarmTypeAndHandleType[1]);
                }

            }
            gpsDealInfo = Strings.join(gpsDealInfoList, ",");
            gpsInfo = Strings.join(gpsInfoList, ",");
        }

    }

    private boolean isGpsAlarm(int alarmType) {
        return alarmType == 4 || alarmType == 5 || alarmType == 6;
    }

    private String getAlarmTypeName(int alarmTypeVal) {
        return StrUtil.getOrBlank(AlarmTypeUtil.getAlarmType(alarmTypeVal + ""));
    }

    @Override
    public void initExportData() {
        alarmStartTimeHour = alarmStartTime.substring(0, 2);
        alarmStartTimeMinute = alarmStartTime.substring(3, 5);
        if ("疲劳".equals(violationReason)) {
            tiredAlarm = "√";
        } else if ("超速".equals(violationReason)) {
            overSpeedAlarm = "√";
        } else {
            otherAlarm = "√";
        }

    }
}
