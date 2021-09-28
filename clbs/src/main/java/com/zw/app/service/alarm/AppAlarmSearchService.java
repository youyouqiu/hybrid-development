package com.zw.app.service.alarm;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.alarm.AppAlarmAction;
import com.zw.app.domain.alarm.AppAlarmDetailInfo;
import com.zw.app.domain.alarm.AppAlarmInfo;
import com.zw.app.domain.alarm.AppAlarmQuery;
import com.zw.app.domain.webMaster.alarmType.AppAlarmConfigInfo;
import com.zw.app.util.common.AppResultBean;

import java.util.List;


public interface AppAlarmSearchService {
    /**
     * 获取用户权限下的报警监控对象
     * @param alarmQuery
     * @return
     */
    List<AppAlarmInfo> getAlarmInfo(AppAlarmQuery alarmQuery) throws Exception;

    /**
     * 获取用户下的报警监控对象数量
     */
    JSONObject getAlarMonitorNumber(AppAlarmQuery alarmQuery) throws Exception;
    /**
     * 根据监控对象id查询监控对象报警概要信息
     */
    List<AppAlarmAction> getMonitorAlarmAction(String vehicleId,AppAlarmQuery alarmQuery) throws Exception;

    /**
     * 根据监控对象id查询监控对象报警详细信息
     */
    List<AppAlarmDetailInfo> getMonitorAlarmDetail(String vehicleId,AppAlarmQuery alarmQuery,String time) throws Exception;

    /**
     * 查询用户的报警参数设置
     */
    JSONObject getUserAlarmSetting() throws Exception;
}
