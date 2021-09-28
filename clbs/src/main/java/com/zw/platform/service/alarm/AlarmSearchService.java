package com.zw.platform.service.alarm;

import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.domain.alarm.AlarmInfo;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.domain.multimedia.HandleMultiAlarms;
import com.zw.platform.domain.oil.AlarmHandle;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.alram.query.AlarmSearchQuery;
import com.zw.platform.domain.vas.alram.query.AlarmSearchQuery809;
import com.zw.platform.dto.alarm.AlarmPageReq;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p> Title: 报警查询Service </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: fanlu
 * @date 2016年12月6日上午11：04
 */
public interface AlarmSearchService {

    List<AlarmType> getAlarmType();

    /**
     * 获得调度报警类型
     * @return List<AlarmType>
     */
    List<AlarmType> getDispatchAlarmType();

    List<AlarmHandle> getAlarmHandle(List<String> vehicleIds, AlarmSearchQuery query);

    List<AlarmHandle> getAlarmList(String vid);

    String getAlarmTime(String vehicleIds);

    String getLatestAlarmHandle(String vehicleId, int type, long startTime);

    /**
     * 根据车辆id查询报警设置参数
     * @param vehicleId
     * @return
     */
    List<AlarmSetting> findSpeedParameter(String vehicleId);

    /**
     * 获取io报警
     * @param vehicleIds
     * @param alarmTypeNames
     * @param status
     * @param startTime
     * @param endTime
     * @param pushType
     * @return JsonResultBean
     */
    JsonResultBean getIoAlarmHandle(String vehicleIds, String alarmTypeNames, Integer status, String startTime,
        String endTime, Integer pushType);

    /**
     * 分页查询
     * @param query
     * @return PageGridBean
     */
    PageGridBean getIoAlarmList(AlarmSearchQuery query);

    /**
     * 开关信号报警
     * @param response
     * @throws Exception
     */
    void exportIoAlarm(HttpServletResponse response) throws Exception;

    /**
     * 处理报警
     * @param handleAlarms
     * @param ip
     * @return
     * @throws Exception
     */
    JsonResultBean updateIoAlarm(HandleAlarms handleAlarms, String ip) throws Exception;

    /**
     * 809报警查询
     * @param alarmType      报警类型
     * @param vehicleIds     监控对象id
     * @param alarmStartTime 开始时间
     * @param alarmEndTime   结束时间
     * @return list
     */
    JsonResultBean find809Alarms(String alarmType, String vehicleIds, String alarmStartTime, String alarmEndTime);

    void export809Alarms(HttpServletResponse response) throws Exception;

    /**
     * 查询调度报警(排班、任务和sos报警)
     * @param alarmType      报警类型
     * @param status         处理状态
     * @param alarmStartTime 查询开始时间
     * @param alarmEndTime   查询结束时间
     * @param monitorIds     监控对象
     * @return JsonResultBean
     */
    JsonResultBean queryDispatchAlarm(String alarmType, Integer status, String alarmStartTime, String alarmEndTime,
        String monitorIds) throws Exception;

    /**
     * 查询调度报警列表(排班、任务和sos报警)
     * @param alarmSearchQuery 查询参数
     * @return PageGridBean
     */
    PageGridBean getDispatchAlarmList(AlarmSearchQuery alarmSearchQuery);

    /**
     * 导出调度报警
     * @param response response
     * @throws Exception Exception
     */
    void exportDispatchAlarm(HttpServletResponse response) throws Exception;

    /**
     * 处理调度报警
     * @param handleAlarms 参数
     * @param ipAddress    ip地址
     * @return JsonResultBean
     */
    JsonResultBean updateDispatchAlarm(HandleAlarms handleAlarms, String ipAddress) throws Exception;

    /**
     * 报警分页查询
     * @param alarmPageReq req
     * @return PageGridBean
     * @throws Exception
     */
    PageGridBean alarmPageList(AlarmPageReq alarmPageReq) throws Exception;

    /**
     * 调用PassCloud接口处理报警
     * @param handleAlarms            处理信息
     * @param needHandleAlarmTypeList 需要处理的报警类型
     * @param isHandleIoAlarm         是否是处理io报警
     * @param startTime               开始时间
     * @param endTime                 结束时间
     * @throws Exception Exception
     */
    void handleAlarmBatch(HandleAlarms handleAlarms, List<String> needHandleAlarmTypeList, boolean isHandleIoAlarm,
        String startTime, String endTime) throws Exception;

    /**
     * 调用PassCloud接口处理单条报警
     * @param handleAlarms 处理信息
     * @param startTimeL   报警开始时间
     * @param alarmType    报警类型
     * @param needTranslationHandleType
     * @throws Exception Exception
     */
    void handleAlarmSingle(HandleAlarms handleAlarms, Long startTimeL, String alarmType,
        Boolean needTranslationHandleType) throws Exception;


    /**
     * 批量处理多条报警
     *
     * @param handleMultiAlarms 报警标识、处理方式等
     */
    void handleAlarmMulti(HandleMultiAlarms handleMultiAlarms) throws Exception;

    /**
     * 查询报警信息
     * @param monitorIds      监控对象id
     * @param alarmTypeStr    报警类型
     * @param startTime       查询开始时间
     * @param endTime         查询结束时间
     * @param alarmSource     报警来源
     * @param status          处理状态
     * @param pushType        推送类型
     * @param limit           返回条数
     * @param inTimeRangeFlag 0: 查询报警开始时间在查询时间范围内; null:查询报警开始时间和结束时间在查询时间范围内
     * @param clazz           返回类型
     * @param sort            接口返回的结果集默认倒序排序,如需正序排序,请传入参数sort=1
     * @return JSONObject
     * @throws Exception Exception
     */
    <T> List<T> getAlarmInfo(String monitorIds, String alarmTypeStr, String startTime, String endTime,
        Integer alarmSource, Integer status, Integer pushType, Integer limit, Integer inTimeRangeFlag, Class<T> clazz,
        Integer sort) throws Exception;

    /**
     * 查询同一报警开始时间报警
     * @param monitorIds     监控对象id
     * @param alarmTypeStr   报警类型
     * @param alarmStartTime 报警开始时间
     * @param limitNum       限制返回条数
     * @return List<AlarmInfo>
     * @throws Exception Exception
     */
    List<AlarmInfo> getTheSameTimeAlarmInfo(String monitorIds, String alarmTypeStr, String alarmStartTime,
        Integer limitNum) throws Exception;

    /**
     * pass层分页查询
     * @param query
     * @return
     */
    PassCloudResultBean getAlarmPage(AlarmSearchQuery809 query) throws Exception;

    /**
     * pass层离线导出
     * @param query
     * @return
     */
    OfflineExportInfo export(AlarmSearchQuery809 query) throws Exception;

    /**
     * 获得809转发报警名称
     * @return JsonResultBean
     */
    JsonResultBean get809ForwardAlarmName();
}
